# repro-metrics-warnings

this is a project to demonstrate a problem when registering Feign prometheus metrics in a Spring Boot application which
already as an observed RestTemplate.

When this is the case the following warning is logged when first calling the Feign client:

```
The meter (MeterId{name='http.client.requests.active', tags=[tag(clientName=com.example.demo.TestFeignClient),tag(http.method=GET),tag(http.status_code=CLIENT_ERROR),tag(http.url=/outside/test)]}) registration has failed: Prometheus requires that all meters with the same name have the same set of tag keys. There is already an existing meter named 'http_client_requests_active_seconds' containing tag keys [client_name, exception, method, outcome, status, uri]. The meter you are attempting to register has keys [clientName, http_method, http_status_code, http_url]. Note that subsequent logs will be logged at debug level.
```

And more importantly the Feign metrics the http_client_requests_active metrics for the feign client are not exported
in Prometheus. The metrics effectively don't look the same : 

```
RestTemplate http_client_requests_active_seconds_count :
http_client_requests_active_seconds_count{client_name="localhost",exception="none",method="GET",outcome="UNKNOWN",status="CLIENT_ERROR",uri="/test"} 0
http_client_requests_active_seconds_sum{client_name="localhost",exception="none",method="GET",outcome="UNKNOWN",status="CLIENT_ERROR",uri="/test"} 0.0

FeignClient http_client_requests_active_seconds_count : 
http_client_requests_active_seconds_count{clientName="com.example.demo.TestFeignClient",http_method="GET",http_status_code="CLIENT_ERROR",http_url="/outside/test"} 0
http_client_requests_active_seconds_sum{clientName="com.example.demo.TestFeignClient",http_method="GET",http_status_code="CLIENT_ERROR",http_url="/outside/test"} 0.0
```

If everything worked correctly the FeignClient metrics should look like this instead, which would be compatible with the RestTemplate metrics:

```
http_client_requests_active_seconds_count{client_name="com.example.demo.TestFeignClient",method="GET",status="CLIENT_ERROR",uri="/outside/test"} 0
http_client_requests_active_seconds_sum{client_name="com.example.demo.TestFeignClient",method="GET",status="CLIENT_ERROR",uri="/outside/test"} 0.0
```



