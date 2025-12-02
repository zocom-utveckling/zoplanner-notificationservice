package com.zoplanner.notification.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.zoplanner.notification.service.SqsMessagePublisher;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MockEventTests {

    @Mock
    private SqsClient sqsClient;

    @InjectMocks
    private SqsMessagePublisher messagePublisher; // service class


    @Test
    public void whenPublicMessage_thenMessageIsSentWithCorrectParameters(){

        //Arrange
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue"; // url from aws sqs
        String messageBody = "Hello SQS";
        String expectedMessageId = "test-message-id-123";

        SendMessageResponse mockResponse = SendMessageResponse.builder()
                .messageId(expectedMessageId)
                .build();
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(mockResponse);

        //Act
        String actualMessageId = messagePublisher.publishMessage(queueUrl,messageBody); //metod from service class

        //Assert

        assertThat(actualMessageId).isEqualTo(expectedMessageId);


        ArgumentCaptor<SendMessageRequest> requestCaptor =
                ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(requestCaptor.capture());

        SendMessageRequest captureRequest = requestCaptor.getValue();
        assertThat(captureRequest.queueUrl()).isEqualTo(queueUrl);
        assertThat(captureRequest.messageBody()).isEqualTo(messageBody);

        System.out.println(actualMessageId);
        System.out.println(captureRequest.queueUrl());
        System.out.println(captureRequest.messageBody());


    }

}
