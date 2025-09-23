package com.sboot.website.model;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
private Boolean status;
private String message;
private Object body;
}