package com.oneline.shimpyo.modules.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Log4j2
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@Aspect
public class RetryAspect {

    @Around("@annotation(retry)")
    public Object doRetry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        //log.info("[retry] "+ joinPoint.getSignature() + "retry = " + retry);
        int maxRetry = retry.value();

        Exception exceptionHolder = null;
        for (int retryCount = 1; retryCount <= maxRetry; retryCount++) {
            try{
                return joinPoint.proceed();
            }catch (Exception e){
                log.error("[retry] try count ={}/{}", retryCount, maxRetry);
                exceptionHolder = e;
            }
        }
        throw exceptionHolder;
    }
}
