package com.StudyLink.www.service;

import com.StudyLink.www.repository.ExchangeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TossPaymentServiceImpl implements TossPaymentService {

    private final ExchangeRequestRepository exchangeRequestRepository;


}
