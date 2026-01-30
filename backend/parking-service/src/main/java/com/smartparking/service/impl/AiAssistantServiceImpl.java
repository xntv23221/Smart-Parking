package com.smartparking.service.impl;

import com.smartparking.dao.mapper.ParkingLotMapper;
import com.smartparking.domain.model.ParkingLot;
import com.smartparking.service.AiAssistantService;
import com.smartparking.service.ai.QwenChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {
    private final QwenChatClient qwenChatClient;
    private final ParkingLotMapper parkingLotMapper;

    public AiAssistantServiceImpl(QwenChatClient qwenChatClient, ParkingLotMapper parkingLotMapper) {
        this.qwenChatClient = qwenChatClient;
        this.parkingLotMapper = parkingLotMapper;
    }

    @Override
    public String ask(String question) {
        List<ParkingLot> lots = parkingLotMapper.selectAll();
        int available = lots.stream().mapToInt(ParkingLot::getAvailableSpots).sum();
        
        String system = "你是智慧停车场助手。请用简洁中文回答用户问题。"
                + " 当前系统统计空闲车位数为：" + available + "。"
                + " 如果用户询问车位，请基于该数值回答，并给出建议。";
        return qwenChatClient.chat(system, question);
    }
}
