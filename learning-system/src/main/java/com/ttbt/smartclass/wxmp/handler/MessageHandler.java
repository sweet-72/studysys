package com.ttbt.smartclass.wxmp.handler;

import java.util.Map;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

/**
 * 消息处理器
 *

    **/
@Component
public class MessageHandler implements WxMpMessageHandler {

    /**
     * 处理微信公众号普通文本消息。
     *
     * @param wxMpXmlMessage 微信入站消息
     * @param map 上下文参数
     * @param wxMpService 微信服务
     * @param wxSessionManager 微信会话管理器
     * @return 微信文本回复
     */
    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map,
            WxMpService wxMpService, WxSessionManager wxSessionManager) throws WxErrorException {
        // 当前实现为复读机模式，将用户输入内容拼接后返回
        String content = "我是复读机：" + wxMpXmlMessage.getContent();
        return WxMpXmlOutMessage.TEXT().content(content)
                .fromUser(wxMpXmlMessage.getToUser())
                .toUser(wxMpXmlMessage.getFromUser())
                .build();
    }
}
