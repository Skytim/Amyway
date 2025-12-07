package com.amyway.luckydraw.service;

import com.amyway.luckydraw.domain.Prize;

import java.util.List;

public interface DrawStrategy {
    Prize draw(List<Prize> prizes);
}
