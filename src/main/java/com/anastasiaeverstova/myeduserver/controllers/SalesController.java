package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.dto.OrderItemDTO;
import com.anastasiaeverstova.myeduserver.dto.SalesDTO;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.repository.OrderItemRepository;
import com.anastasiaeverstova.myeduserver.repository.SalesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/sales", produces = MediaType.APPLICATION_JSON_VALUE)
public class SalesController {

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @GetMapping(path = "/mine")
    public Slice<SalesDTO> getAllMyOwnedItems(@AuthenticationPrincipal User user,
                                              @RequestParam(defaultValue = "0") Integer page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC, "createdAt");
        return salesRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    @GetMapping(path = "/mine/{transactionId}")
    public Slice<OrderItemDTO> getItemsbyTransactionId(@PathVariable String transactionId,
                                                       @RequestParam(defaultValue = "0") Integer page) {
        return orderItemRepository.findByTransactionIdEquals(transactionId, PageRequest.of(page, 10));
    }

}
