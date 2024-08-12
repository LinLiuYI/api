package com.LinYi.backend.service.inner.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.LinYi.backend.common.ErrorCode;
import com.LinYi.backend.exception.BusinessException;
import com.LinYi.backend.model.entity.User;
import com.LinYi.backend.service.UserService;
import com.LinYi.common.model.vo.UserVO;
import com.LinYi.common.service.inner.InnerUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;

/**
 * @author: LinYi
 * @Date: 2023年09月15日 22:54
 * @Version: 1.0
 * @Description:
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {
    @Resource
    private UserService userService;

    @Override
    public UserVO getInvokeUserByAccessKey(String accessKey) {
        if (StringUtils.isAnyBlank(accessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getAccessKey, accessKey);
        User user = userService.getOne(userLambdaQueryWrapper);
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
