package com.example.selenium.service.selenium;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.common.util.CommonUtils;
import com.example.selenium.model.selenium.RequestParam;
import com.example.selenium.model.selenium.SeleniumConfig;
import com.example.selenium.repository.selenium.SeleniumConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.example.selenium.common.util.CommonUtils.*;
import static com.example.selenium.common.util.Commons.*;


@Service
@RequiredArgsConstructor
public class SeleniumConfigServiceImpl implements SeleniumConfigService{

    private final SeleniumConfigMapper seleniumConfigMapper;
    private static List<SeleniumConfig> seleniumConfigList;
    private static Map<String, Object> seleniumParams = new ConcurrentHashMap<>();

    @Override
    public void loadConfigAll() {
        seleniumConfigList = selectSeleniumConfigAll();

        if (!CommonUtils.isEmpty(seleniumConfigList)) {

            List<SeleniumConfig> countries = getConfig(seleniumConfigList, CONFIG_CATEGORY_SEND_AMOUNT.CODE());
            List<SeleniumConfig> pCountryNames = getConfig(seleniumConfigList, CONFIG_CATEGORY_P_COUNTRY_NAME.CODE());
            List<SeleniumConfig> payoutCountries = getConfig(seleniumConfigList, CONFIG_CATEGORY_PAYOUT_COUNTRY.CODE());
            List<SeleniumConfig> paymentTypes = getConfig(seleniumConfigList, CONFIG_CATEGORY_PAYMENT_TYPE.CODE());
            List<SeleniumConfig> deliveryMethods = getConfig(seleniumConfigList, CONFIG_CATEGORY_DELIVERY_METHOD.CODE());
            List<SeleniumConfig> currencies = getConfig(seleniumConfigList, CONFIG_CATEGORY_CURRENCY.CODE());
            List<SeleniumConfig> countryKoCodes = getConfig(seleniumConfigList, CONFIG_CATEGORY_COUNTRY_KO_CODE.CODE());
            List<SeleniumConfig> countryEnCodes = getConfig(seleniumConfigList, CONFIG_CATEGORY_COUNTRY_EN_CODE.CODE());
            List<SeleniumConfig> countryNoCodes = getConfig(seleniumConfigList, CONFIG_CATEGORY_COUNTRY_NO_CODE.CODE());
            List<SeleniumConfig> currencyNoCodes = getConfig(seleniumConfigList, CONFIG_CATEGORY_CURRENCY_NO_CODE.CODE());

            seleniumParams.put(CONFIG_CATEGORY_COUNTRY.CODE(), countries);
            seleniumParams.put(CONFIG_CATEGORY_P_COUNTRY_NAME.CODE(), pCountryNames);
            seleniumParams.put(CONFIG_CATEGORY_PAYOUT_COUNTRY.CODE(), payoutCountries);
            seleniumParams.put(CONFIG_CATEGORY_PAYMENT_TYPE.CODE(), paymentTypes);
            seleniumParams.put(CONFIG_CATEGORY_DELIVERY_METHOD.CODE(), deliveryMethods);
            seleniumParams.put(CONFIG_CATEGORY_CURRENCY.CODE(), currencies);
            seleniumParams.put(CONFIG_CATEGORY_COUNTRY_KO_CODE.CODE(), countryKoCodes);
            seleniumParams.put(CONFIG_CATEGORY_COUNTRY_EN_CODE.CODE(), countryEnCodes);
            seleniumParams.put(CONFIG_CATEGORY_COUNTRY_NO_CODE.CODE(), countryNoCodes);
            seleniumParams.put(CONFIG_CATEGORY_CURRENCY_NO_CODE.CODE(), currencyNoCodes);
        }
    }

    @Override
    public List<SeleniumConfig> selectSeleniumConfigAll() {
        return seleniumConfigMapper.selectSeleniumConfigAll();
    }

    public List<SeleniumConfig> getConfigAll() {
        return seleniumConfigList;
    }

    public List<RequestParam> getParameters(String name) {

        if (isEmpty(name)) {
            Logger.error("파라미터가 부족합니다. name={}", name);
            throw new NullPointerException("parameter is null");
        }

        if (isEmpty(seleniumParams)) {
            Logger.error("selenium 설정값 없음.");
            throw new NullPointerException("parameter is null");
        }

        List<SeleniumConfig> countries          = (List<SeleniumConfig>) seleniumParams.get(CONFIG_CATEGORY_COUNTRY.CODE());
        List<SeleniumConfig> pCountryNames      = (List<SeleniumConfig>) seleniumParams.get(CONFIG_CATEGORY_P_COUNTRY_NAME.CODE());
        List<SeleniumConfig> payoutCountries    = (List<SeleniumConfig>) seleniumParams.get(CONFIG_CATEGORY_PAYOUT_COUNTRY.CODE());
        List<SeleniumConfig> paymentTypes       = (List<SeleniumConfig>) seleniumParams.get(CONFIG_CATEGORY_PAYMENT_TYPE.CODE());
        List<SeleniumConfig> deliveryMethods    = (List<SeleniumConfig>) seleniumParams.get(CONFIG_CATEGORY_DELIVERY_METHOD.CODE());
        List<SeleniumConfig> currencies         = (List<SeleniumConfig>) seleniumParams.get(CONFIG_CATEGORY_CURRENCY.CODE());
        List<SeleniumConfig> countryKoCodes     = (List<SeleniumConfig>) seleniumParams.get(CONFIG_CATEGORY_COUNTRY_KO_CODE.CODE());
        List<SeleniumConfig> countryEnCodes     = (List<SeleniumConfig>) seleniumParams.get(CONFIG_CATEGORY_COUNTRY_EN_CODE.CODE());
        List<SeleniumConfig> countryNoCodes     = (List<SeleniumConfig>) seleniumParams.get(CONFIG_CATEGORY_COUNTRY_NO_CODE.CODE());
        List<SeleniumConfig> currencyNoCodes    = (List<SeleniumConfig>) seleniumParams.get(CONFIG_CATEGORY_CURRENCY_NO_CODE.CODE());


        List<SeleniumConfig> countriesByCodeParam = countries.stream().filter(x-> x.getCodeParam().equalsIgnoreCase(name)).collect(Collectors.toList());

        List list = new ArrayList();

        for (SeleniumConfig config : countriesByCodeParam) {

            String country = config.getCode();
            if (isEmpty(country)) {
                Logger.error("{}, 설정값이 부족합니다. country={}", name, country);
                continue;
            }

            country = name.equalsIgnoreCase(COMPETITOR4.CODE()) ?    getItem(pCountryNames, country, name)     : country;
            country = name.equalsIgnoreCase(COMPETITOR5.CODE()) ? getItem(payoutCountries, country, name)   : country;

            if (isEmpty(countries)) continue;

            String currency         = getItem(currencies, country, name);
            String sendAmount       = getItem(countriesByCodeParam, country, name);
            String countryKoCode    = getItem(countryKoCodes, country, CONFIG_CODE_PARAM_COMMON.CODE());
            String countryEnCode    = getItem(countryEnCodes, country, name);

            String paymentType      = name.equalsIgnoreCase(COMPETITOR5.CODE())
                    || name.equalsIgnoreCase(COMPETITOR6.CODE()) ? getItem(paymentTypes, country, name) : "";

            String deliveryMethod   = name.equalsIgnoreCase(COMPETITOR4.CODE()) ? getItem(deliveryMethods, country, name) : "";
            String centerCode       = name.equalsIgnoreCase(COMPETITOR6.CODE()) ? getItem(countries, country, name) : "";

            String countryNoCode    = name.equalsIgnoreCase(COMPETITOR12.CODE())
                    || name.equalsIgnoreCase(COMPETITOR11.CODE()) ? getItem(countryNoCodes, country, name) : "";

            String currencyNoCode    = name.equalsIgnoreCase(COMPETITOR12.CODE())
                    || name.equalsIgnoreCase(COMPETITOR11.CODE()) ? getItem(currencyNoCodes, currency, name) : "";


            if (isEmpty(currency) || isEmpty(sendAmount) || isEmpty(countryEnCode)) {
                Logger.error("{}, 설정값이 부족합니다. curreny={}, sendAmount={}", name, currency, sendAmount);
                continue;
            }

            if (name.equals(COMPETITOR8.CODE()) && isEmpty(countryKoCode)) {
                Logger.error("{}, 설정값이 부족합니다. countryKoCode={}", name, countryKoCode);
                continue;
            } else if (name.equals(COMPETITOR4.CODE()) && isEmpty(deliveryMethod)) {
                Logger.error("{}, 설정값이 부족합니다. deliveryMethod={}", name, deliveryMethod);
                continue;
            } else if ((name.equals(COMPETITOR12.CODE()) || name.equals(COMPETITOR11.CODE()))) {
                if (isEmpty(countryNoCode) || isEmpty(currencyNoCode)) {
                    Logger.error("{}, 설정값이 부족합니다. countryNoCode={}, currencyNoCode={}", name, countryNoCode, currencyNoCode);
                    continue;
                }
            }

            RequestParam requestParam = new RequestParam();
            requestParam.setCountry(country);
            requestParam.setCompany(name);
            requestParam.setCurrency(currency);
            requestParam.setSendAmount(sendAmount);
            requestParam.setKoCountryName(countryKoCode);
            requestParam.setCountryEnCode(countryEnCode);
            requestParam.setDeliveryMethod(deliveryMethod);
            requestParam.setPaymentType(paymentType);
            requestParam.setCenterCode(centerCode);
            requestParam.setCountryNoCode(countryNoCode);
            requestParam.setCurrencyNoCode(currencyNoCode);

            list.add(requestParam);
        }

        return list;
    }

    @Override
    public Map<String, Object> getMessageTarget() {
        if (isEmpty(seleniumConfigList)) {
            Logger.error("메세지 대상을 가져올 수 없음. seleniumConfigList size={}", seleniumConfigList.size());
            return null;
        }

        List<SeleniumConfig> configs = seleniumConfigList.stream().filter(x-> x.getCategory().equalsIgnoreCase(CONFIG_CATEGORY_CONFIG.CODE())).collect(Collectors.toList());
        List<SeleniumConfig> messageTarget = configs.stream().filter(x-> x.getCode().equalsIgnoreCase(CONFIG_CODE_MAIL.CODE()) || x.getCode().equalsIgnoreCase(CONFIG_CODE_SMS.CODE())).collect(Collectors.toList());

        Map<String, Object> map = new ConcurrentHashMap<>();
        for (SeleniumConfig item : messageTarget) {
            if (item.getCode().equalsIgnoreCase(CONFIG_CODE_MAIL.CODE())) {
                map.put(CONFIG_CODE_MAIL.CODE(), item.getCodeValue().split(","));
            }

            if (item.getCode().equalsIgnoreCase(CONFIG_CODE_SMS.CODE())) {
                map.put(CONFIG_CODE_SMS.CODE(), item.getCodeValue().split(","));
            }
        }

        return map;
    }
}
