package com.oneline.shimpyo.controller;

import com.oneline.shimpyo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
}
