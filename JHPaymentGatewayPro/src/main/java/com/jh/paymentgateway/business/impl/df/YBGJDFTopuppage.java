package com.jh.paymentgateway.business.impl.df;

import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.df.ybgjdf.YBGJDFTopupRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class YBGJDFTopuppage extends BaseChannel implements TopupRequestBusiness {

    private static final Logger LOG = LoggerFactory.getLogger(YBGJDFTopuppage.class);

    @Autowired
    private YBGJDFTopupRequest ybgjdfTopupRequest;



    public final String myPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6n5kl0KbwksRGSTvLvxTmMSWhzxluYUU2Zm4YRcpik1TvId96j3kN8I+5z0oVah+RSl1eGR/+K8LbFfqYEXwgo6nGhoI+GEsbInKlnBGSu9skmJMWhfLIPjBU9CG31OLllK/Vfw+Hz3ubmTasymH/GfdXmf4TTGO5l2uwtIVl4yej/WwYsXBxNZzSVSvV8GEigf2hUBdZxY2rsH2DOjPOs2rbhsXydPdcfs9ZCXb0Bi5Tn6ex/mnAQuquoTdjQKK8+ZQ/AGZxOlC2pcGjXiuy/FMrSYY0Zqb/KouCfX/nvRFcQ+WXjW0r/Lfu5KXBVGLL75PCX9ragfgxIgL1Hz88wIDAQAB";
    public final String myPrivateKey = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDKjlt3PWoBbchL2nLv1QP18vTmtS9gZiDIzUFJlB6Z8tvRpO27qvirJMMTSollDtUmNw1uFdkgWzOuimbjd1fNvxJ3Hb5LJ1caWQaqNslIwlkuwOQJKnvCpCtqFLwuMT7T5JvA486rXn7aGtZ+1pjr0ZbSUJfwJkEJlr7Wv5FB2UCMNV5nhvP6iAm+BAr/BUnTM4LH17BCFOSNs9pPZw3xONiivwuR5tOfWJzMDF8G9G8GfwcMdI+Jgydmd1LETQ874rWiFGcqkiq/YXUfVDMYR6v48vZ9GroEbvV2Vdlervmrct96INt8PPFyyOvJTIkH7/TgLaItyKA1KdTbW8FbAgMBAAECggEBAIx9BkWCzBZnnWe3AMcFeLTKqT+m5VA4JX8AlcrBuCPQxNP4T4c9gzG40rB7nyX/jqjtwBvdfXYqq4LgrZIRAU5LuzPw5j5b79bvbmb6jssvOQnrcX5GAAa1NQNjlE15jHkM81Zq6roDVXpS6PiaQQA2oyQGEAvZjHxpGL37qnT/S8NV1Uhzz7GO3UPfyrxM7HnWjQ5aHr5nju6JMLYJYIOCq/48vUQloLOPTSP4mTaWPrxAhCCeXshe/ie1VfnpBZtKvQRbBIU5zl1Z3jqycGJkvCIc9/ugD8MVsmjRdjkAJcMfgCZqNr2TcmANFUgMzlnWyLxrs2LkBOJMxnXJAYECgYEA+bMdwqVDtmlWgOc28pppO/o28nnEVkCAPgBM/iF9UrfIlY0K6gO6jfHI6yExx20sI0q2cYgkfT8SYYjtO17rrnk1Mt1zAZIkNyQwJnRZEbjumxF6r26T6HOswmto9t7rcI04TlTGkjbIhkdhl1YDNgWJuRE7bz459i4lR6EwwEECgYEAz6q6CSy0s2hqQq2DV2+8Dlml7wjqEaNsk9Hp6Iu6vD0e6sPg7iuO5k46uI8yZhbZMJoGNOZsj6o5aG7hVm1meNLw3fuGWlu7cZ1MMlGzG82r4ArTnDmsNmkEWuQzaGEIqdaRApikY/TmZ2BellUCM6n6IYAbkRIEURYBnPcg2psCgYEA7RagdFfdMk9UnnJr3vC0aQmMsLDjto3p9nTspgQAbdEpOK7CJS7DLNHd1zweet/zH9Np5eTB2NZRSqDruArJt1FJLPHNqDkt/9lxiqHAjK5OiJiRKA/TisyCxducv4MshCLiyqV+Igi4X59ZT4uQ4vNhMILnLxojiNeJ+DhaBMECgYEAmC+pvmeA1anDlbHK+xzrWCjirdZ+kwaM35eKL92Wr7SrH6kFAgXx512VMwPeD2rXKAL4YXQsQuygvaPkh44M9qa/1i4qO7XWHGEGIio1dD65oqrMgUTSq90yT9nEws25p52TD7AFaStkXoYFS25o+quqnzpe+WWWvEXAUOPas9kCgYEAu59klu0krgeYrWM/ULvM/yvhyN1KMuWBjlMHVacCCSraoh9rlafar650aEedj3j1pDOMATT4fi6QrTao3MkWdPnCaWPUNW9CXJ3L6TS2Ru8nC0wErdevnMZ55kqfXYoT00ZRGcX43RhaTYJ6HLmpCD7xfYeqHAYMeP8T44aIWT8=";
    public final String partnerNo = "YBGJuwBsg2J4";


    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
        Map<String, Object> map = new HashMap<String, Object>();
        String orderCode = bean.getOrderCode();
        LOG.info("进入ybgj代付通道=====================");
        map = (Map<String, Object>)ybgjdfTopupRequest.pay(orderCode);
        return map;
    }


}
