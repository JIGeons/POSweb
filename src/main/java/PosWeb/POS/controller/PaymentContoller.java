package PosWeb.POS.controller;

import PosWeb.POS.component.ImpProperties;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;


//== iamport 결제 controller ==//
@Controller
@Slf4j
@RequiredArgsConstructor
@Tag(name = "결제 컨트롤러", description = "결제 API")
public class PaymentContoller {

    private final ImpProperties impProperties;

    private IamportClient iamportClient;
    private String apiKey;
    private String secretKey;

    @PostConstruct
    public void init() {
        apiKey = impProperties.getApiKey();
        secretKey = impProperties.getSecretKey();

        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    @Operation(summary = "결제 요청", description = "iamport로 요청한 주문에 대해 결과를 출력하는 API")
    @PostMapping("/payment/validation/{imp_uid}")
    @ResponseBody
    public IamportResponse<Payment> validateIamport(@PathVariable String imp_uid) {
        IamportResponse<Payment> payment = iamportClient.paymentByImpUid(imp_uid);
        log.info("결제 요청 응답. 결제 내역 - 주문 번호: {}", payment.getResponse().getMerchantUid());
        return payment;
    }

}
