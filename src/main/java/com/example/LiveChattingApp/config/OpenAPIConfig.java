package com.example.LiveChattingApp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
  info=@Info(
    contact = @Contact(
      name="Alexandru",
      email= "alexandru.ioan39@gmail.com"
    ),
    description = "OpenApi documentation for Spring application",
    title = "OpenApi specification - Alexandru",
    version = "1.0"
  ),
  security = {
    @SecurityRequirement(
      name="bearerAuth"
    )
  }
)

@SecurityScheme(
  name ="bearerAuth",
  description = "JWT authentication",
  scheme="bearer",
  type= SecuritySchemeType.HTTP,
  bearerFormat = "JWT",
  in = SecuritySchemeIn.HEADER
)
public class OpenAPIConfig{


}
