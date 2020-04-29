package com.jh.user;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableSwagger2
@Configuration
public class SwaggerTest {


    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()) //创建Api的基本信息
                .select()   //返回一个ApiSelectorBuilder实例，用来控制那些接口暴露给Swagger来展示
                .apis(RequestHandlerSelectors.basePackage("com.jh.user.service"))
                .paths(PathSelectors.any())
                .build();

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("测试所用的APi")
                .description("ives，:http://www.baidu.com")
                .termsOfServiceUrl("http://blog.didispace.com/")
                .contact("ives")
                .version("1.0")
                .build();
    }





}
