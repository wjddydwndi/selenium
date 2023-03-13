package com.example.selenium.repository.biztalk;

import com.example.selenium.model.biztalk.ImcMtMsg;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageMapper {
    void insert(ImcMtMsg imcMtMsg);
}
