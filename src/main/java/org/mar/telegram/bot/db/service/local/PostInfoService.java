//package org.mar.telegram.bot.db.service.local;
//
//import org.mar.telegram.bot.db.entity.PostInfo;
//import org.mar.telegram.bot.db.jpa.PostInfoRepository;
//import org.mar.telegram.bot.service.bot.db.PostService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//
//import static java.util.Objects.isNull;
//
//@Service
//@Profile("!local")
//public class PostInfoService implements PostService {
//
//    @Autowired
//    private PostInfoRepository postInfoRepository;
//
//    public PostInfo getNotSendPost() {
//        PostInfo postInfo = postInfoRepository.getByIsSend(false);
//        if (isNull(postInfo)) {
//            return postInfoRepository.save(PostInfo.builder().isSend(false).build());
//        }
//        return postInfo;
//    }
//
//    public PostInfo save(PostInfo postInfo) {
//        return postInfoRepository.save(postInfo);
//    }
//
//    public PostInfo getByChatIdAndMessageId(Long chatId, Integer messageId) {
//        return postInfoRepository.getByChatIdAndMessageId(chatId, messageId);
//    }
//
//}
