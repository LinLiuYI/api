package com.LinYi.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.LinYi.backend.mapper.RechargeActivityMapper;
import com.LinYi.backend.model.entity.RechargeActivity;
import com.LinYi.backend.service.RechargeActivityService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: LinYi
 * @Date: 2023/09/11 11:26:46
 * @Version: 1.0
 * @Description: 充值活动服务impl
 */
@Service
public class RechargeActivityServiceImpl extends ServiceImpl<RechargeActivityMapper, RechargeActivity>
        implements RechargeActivityService {

    @Override
    public List<RechargeActivity> getRechargeActivityByOrderNo(String orderNo) {
        LambdaQueryWrapper<RechargeActivity> activityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        activityLambdaQueryWrapper.eq(RechargeActivity::getOrderNo, orderNo);
        return this.list(activityLambdaQueryWrapper);
    }
}




