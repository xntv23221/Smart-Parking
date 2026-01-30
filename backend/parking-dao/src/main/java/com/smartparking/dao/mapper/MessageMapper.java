package com.smartparking.dao.mapper;

import com.smartparking.domain.model.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MessageMapper extends CrudMapper<Message, Long> {
    List<Message> selectByReceiver(@Param("receiverId") Long receiverId, @Param("receiverRole") String receiverRole);
    List<Message> selectRelatedMessages(@Param("userId") Long userId, @Param("userRole") String userRole);
    List<Message> selectConversation(@Param("userId") Long userId, @Param("userRole") String userRole, 
                                     @Param("otherId") Long otherId, @Param("otherRole") String otherRole);
    int updateReadStatus(@Param("messageId") Long messageId);
    int markAllRead(@Param("receiverId") Long receiverId, @Param("receiverRole") String receiverRole);
    int markConversationRead(@Param("receiverId") Long receiverId, @Param("receiverRole") String receiverRole,
                             @Param("senderId") Long senderId, @Param("senderRole") String senderRole);
}
