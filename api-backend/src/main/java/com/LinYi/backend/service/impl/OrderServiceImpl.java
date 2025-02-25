package com.LinYi.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.LinYi.backend.common.ErrorCode;
import com.LinYi.backend.exception.BusinessException;
import com.LinYi.backend.model.entity.ProductInfo;
import com.LinYi.backend.model.entity.ProductOrder;
import com.LinYi.backend.model.entity.RechargeActivity;
import com.LinYi.backend.model.enums.PaymentStatusEnum;
import com.LinYi.backend.model.enums.ProductTypeStatusEnum;
import com.LinYi.backend.model.vo.ProductOrderVo;
import com.LinYi.backend.model.vo.UserVO;
import com.LinYi.backend.service.OrderService;
import com.LinYi.backend.service.ProductOrderService;
import com.LinYi.backend.service.RechargeActivityService;
import com.LinYi.backend.utils.RedissonLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.LinYi.backend.model.enums.PayTypeStatusEnum.ALIPAY;
import static com.LinYi.backend.model.enums.PayTypeStatusEnum.WX;

/**
 * @author: LinYi
 * @Date: 2023/08/25 06:22:02
 * @Version: 1.0
 * @Description: 订单服务
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private ProductOrderService productOrderService;

    @Resource
    private List<ProductOrderService> productOrderServices;

    @Resource
    private RechargeActivityService rechargeActivityService;

    @Resource
    private ProductInfoServiceImpl productInfoService;

    @Resource
    private RedissonLockUtil redissonLockUtil;

    /**
     * 按付费类型获取产品订单服务
     *
     * @param payType 付款类型
     * @return {@link ProductOrderService}
     */
    @Override
    public ProductOrderService getProductOrderServiceByPayType(String payType) {
        return productOrderServices.stream()
                .filter(s -> {
                    Qualifier qualifierAnnotation = s.getClass().getAnnotation(Qualifier.class);
                    return qualifierAnnotation != null && qualifierAnnotation.value().equals(payType);
                })
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "暂无该支付方式"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductOrderVo createOrderByPayType(Long productId, String payType, UserVO loginUser) {
        // 按付费类型获取产品订单服务Bean
        ProductOrderService productOrderService = getProductOrderServiceByPayType(payType);
        String redissonLock = ("getOrder_" + loginUser.getUserAccount()).intern();

        ProductOrderVo getProductOrderVo = redissonLockUtil.redissonDistributedLocks(redissonLock, () -> {
            // 订单存在就返回不再新创建
            return productOrderService.getProductOrder(productId, loginUser, payType);
        });
        if (getProductOrderVo != null) {
            return getProductOrderVo;
        }
        redissonLock = ("createOrder_" + loginUser.getUserAccount()).intern();
        // 分布式锁工具
        return redissonLockUtil.redissonDistributedLocks(redissonLock, () -> {
            // 检查是否购买充值活动
            checkBuyRechargeActivity(loginUser.getId(), productId);
            // 保存订单,返回vo信息
            return productOrderService.saveProductOrder(productId, loginUser);
        });
    }

    /**
     * 检查购买充值活动
     *
     * @param userId    用户id
     * @param productId 产品订单id
     */
    private void checkBuyRechargeActivity(Long userId, Long productId) {
        ProductInfo productInfo = productInfoService.getById(productId);
        if (productInfo.getProductType().equals(ProductTypeStatusEnum.RECHARGE_ACTIVITY.getValue())) {
            LambdaQueryWrapper<ProductOrder> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderLambdaQueryWrapper.eq(ProductOrder::getUserId, userId);
            orderLambdaQueryWrapper.eq(ProductOrder::getProductId, productId);
            orderLambdaQueryWrapper.eq(ProductOrder::getStatus, PaymentStatusEnum.NOTPAY.getValue());
            orderLambdaQueryWrapper.or().eq(ProductOrder::getStatus, PaymentStatusEnum.SUCCESS.getValue());

            long orderCount = productOrderService.count(orderLambdaQueryWrapper);
            if (orderCount > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该商品只能购买一次，请查看是否已经创建了该订单，或者挑选其他商品吧！");
            }
            LambdaQueryWrapper<RechargeActivity> activityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            activityLambdaQueryWrapper.eq(RechargeActivity::getUserId, userId);
            activityLambdaQueryWrapper.eq(RechargeActivity::getProductId, productId);
            long count = rechargeActivityService.count(activityLambdaQueryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该商品只能购买一次，请查看是否已经创建了该订单，或者挑选其他商品吧！！");
            }
        }
    }

    /**
     * 查找超过minutes分钟并且未支付的的订单
     *
     * @param minutes 分钟
     * @return {@link List}<{@link ProductOrder}>
     */
    @Override
    public List<ProductOrder> getNoPayOrderByDuration(int minutes, Boolean remove, String payType) {
        Instant instant = Instant.now().minus(Duration.ofMinutes(minutes));
        LambdaQueryWrapper<ProductOrder> productOrderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productOrderLambdaQueryWrapper.eq(ProductOrder::getStatus, PaymentStatusEnum.NOTPAY.getValue());
        if (StringUtils.isNotBlank(payType)) {
            productOrderLambdaQueryWrapper.eq(ProductOrder::getPayType, payType);
        }
        // 删除
        if (remove) {
            productOrderLambdaQueryWrapper.or().eq(ProductOrder::getStatus, PaymentStatusEnum.CLOSED.getValue());
        }
        productOrderLambdaQueryWrapper.and(p -> p.le(ProductOrder::getCreateTime, instant));
        return productOrderService.list(productOrderLambdaQueryWrapper);
    }

    /**
     * 做订单通知
     * 支票支付类型
     *
     * @param notifyData 通知数据
     * @param request    要求
     * @return {@link String}
     */
    @Override
    public String doOrderNotify(String notifyData, HttpServletRequest request) {
        String payType;
        if (notifyData.startsWith("gmt_create=") && notifyData.contains("gmt_create") && notifyData.contains("sign_type") && notifyData.contains("notify_type")) {
            payType = ALIPAY.getValue();
        } else {
            payType = WX.getValue();
        }
        return this.getProductOrderServiceByPayType(payType).doPaymentNotify(notifyData, request);
    }
}
