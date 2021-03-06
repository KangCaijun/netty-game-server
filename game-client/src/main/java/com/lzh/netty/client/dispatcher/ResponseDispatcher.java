package com.lzh.netty.client.dispatcher;

import com.lzh.game.common.bean.HandlerMethod;
import com.lzh.game.common.serialization.SerializationUtil;
import com.lzh.netty.client.support.ExchangeProtocol;
import com.lzh.netty.client.support.ActionMethodSupport;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;

import java.util.Objects;

@Slf4j
public class ResponseDispatcher {

    private ActionMethodSupport methodSupport;

    public ResponseDispatcher(ActionMethodSupport methodSupport) {
        this.methodSupport = methodSupport;
    }

    public void doResponse(Channel channel, ExchangeProtocol.Response response) {
        //SerializationUtil.deSerialize()
        int cmd = response.getHead().getCmd();
        HandlerMethod method = methodSupport.getMethod(cmd);
        if (Objects.isNull(method)) {
            throw new IllegalArgumentException("Not register the " + cmd + " protocol !!");
        }

        try {
            Object[] params = getArgumentValues(response.getData().toByteArray(), method);
            method.doInvoke(params);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{} response error", cmd);
        }
        // response.getData().toByteArray();
    }

    private Object[] getArgumentValues(byte[] data, HandlerMethod method) {
        MethodParameter[] parameters = method.getMethodParameters();
        if (parameters.length <= 0 && data.length > 0 || parameters.length > 0 && data.length <= 0) {
            throw new IllegalArgumentException("Not have response data");
        }
        // Default use the first param to parse the response data
        if (parameters.length > 0) {
            MethodParameter defaultValue = parameters[0];

            return new Object[]{SerializationUtil.deSerialize(data, defaultValue.getParameterType())};
        }
        return null;
    }

}
