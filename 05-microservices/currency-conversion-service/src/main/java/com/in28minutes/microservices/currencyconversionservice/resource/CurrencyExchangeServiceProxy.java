package com.in28minutes.microservices.currencyconversionservice.resource;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "currency-exchange-service", url = "${CURRENCY_EXCHANGE_URI:http://localhost:8000}")
//@FeignClient(name = "currency-exchange-service") // will be used when only naming server eureka being used
@FeignClient(name="netflix-zuul-api-gateway-server") //will be used once API gateway zuul and eureka implemented.
public interface CurrencyExchangeServiceProxy {

	//@GetMapping("/currency-exchange/from/{from}/to/{to}") //will be used - in case of no Zuul-api-gateway
	@GetMapping("/currency-exchange-service/currency-exchange/from/{from}/to/{to}")
	//Zuul with ask naming server to resolve into the service with its name
	//thats why we had to add service in url context.
	public CurrencyConversionBean retrieveExchangeValue(@PathVariable("from") String from,
			@PathVariable("to") String to);
}