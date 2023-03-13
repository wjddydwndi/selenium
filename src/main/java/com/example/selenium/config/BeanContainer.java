package com.example.selenium.config;


import com.example.selenium.fxrate.ICompetitor;
import com.example.selenium.fxrate.competitors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BeanContainer {

    private final Competitor1 competitor1;
    private final Competitor2 competitor2;
    private final Competitor3 competitor3;
    private final Competitor4 competitor4;
    private final Competitor5 competitor5;
    private final Competitor6 competitor6;
    private final Competitor7 competitor7;
    private final Competitor8 competitor8;
    private final Competitor9 competitor9;
    private final Competitor10 competitor10;
    private final Competitor11 competitor11;
    private final Competitor12 competitor12;
    private final Competitor13 competitor13;



    @Bean
    public List<ICompetitor> callableList() {
        List<ICompetitor> list = new ArrayList<>();

        list.add(competitor1);
        list.add(competitor2);
        list.add(competitor3);
        list.add(competitor4);
        list.add(competitor5);
        list.add(competitor6);
        list.add(competitor7);
        list.add(competitor8);
        list.add(competitor9);
        list.add(competitor10);
        list.add(competitor11);
        list.add(competitor12);
        list.add(competitor13);

        return list;
    }
}
