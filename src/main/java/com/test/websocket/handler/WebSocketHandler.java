package com.test.websocket.handler;

import com.test.websocket.common.exception.InvalidXmlException;
import com.test.websocket.domain.entity.Test;
import com.test.websocket.repository.TestJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class WebSocketHandler implements org.springframework.web.socket.WebSocketHandler {

    private final TestJpaRepository testJpaRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //TODO: session 에서 token으로 사용자 가져오기.
        session.sendMessage(new TextMessage(session.getId() + " welcome!!! soyun service! :)"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String xml = message.getPayload().toString();
        log.info("Get Message({})= {}", xml.length(), xml);

        if(xml.length() != Integer.parseInt(Objects.requireNonNull(
                session.getHandshakeHeaders().getFirst("soyun"),
                "헤더 값을 찾을 수 없습니다."))) throw new InvalidXmlException("데이터 길이가 올바르지 않습니다.");

        try {
            // XML to Object
            JAXBContext jaxbContext = JAXBContext.newInstance(Test.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Test test = (Test) unmarshaller.unmarshal(new StringReader(xml));

            testJpaRepository.save(test);

            session.sendMessage(new TextMessage(xml));
        } catch (Exception e) {
            throw new InvalidXmlException();
        }

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("ERROR!!!");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("END!!!");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}