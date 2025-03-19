package com.microservices.smmsb_api_gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SwaggerIndexController {

    @GetMapping("/swagger-index")
    @ResponseBody
    public String getSwaggerIndex() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>SMMSB Microservices API Documentation</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; line-height: 1.6; }
                        .container { max-width: 800px; margin: 0 auto; }
                        h1 { color: #333; border-bottom: 1px solid #eee; padding-bottom: 10px; }
                        h2 { color: #495057; margin-top: 0; }
                        h3 { color: #6c757d; font-size: 1rem; margin: 15px 0 5px; }
                        ul { padding-left: 20px; }
                        li { margin-bottom: 10px; }
                        a { color: #0275d8; text-decoration: none; }
                        a:hover { text-decoration: underline; }
                        .service { background: #f8f9fa; padding: 15px; border-radius: 5px; margin-bottom: 20px; border-left: 4px solid #0275d8; }
                        .access-path { background: #e9ecef; padding: 10px 15px; border-radius: 4px; margin-top: 10px; }
                        .badge { display: inline-block; padding: 3px 7px; font-size: 12px; font-weight: 700; border-radius: 3px; margin-right: 5px; }
                        .badge-primary { background-color: #0275d8; color: white; }
                        .footer { margin-top: 30px; text-align: center; font-size: 0.9rem; color: #6c757d; border-top: 1px solid #eee; padding-top: 20px; }
                        code { background: #f1f3f5; padding: 2px 5px; border-radius: 3px; font-family: monospace; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>SMMSB Microservices API Documentation</h1>

                        <div class="service">
                            <h2>User Service</h2>
                            <p>Manages user accounts, authentication, and user-related operations.</p>

                            <div class="access-path">
                                <a href="/user-service/swagger-ui.html" target="_blank" class="badge badge-primary">View API Documentation</a>
                                <code>/user-service/swagger-ui.html</code>
                            </div>
                        </div>

                        <div class="service">
                            <h2>Inventory Service</h2>
                            <p>Handles product stock and inventory management.</p>
                            <div class="access-path">
                      
                                <a href="/inventory-service/swagger-ui.html" target="_blank" class="badge badge-primary">View API Documentation</a>
                                <code>/inventory-service/swagger-ui.html</code>
                            </div>
                        </div>

                        <div class="service">
                            <h2>Transaction Service</h2>
                            <p>Manages all transaction-related operations.</p>
                            <div class="access-path">
                                <a href="/transaction-service/swagger-ui.html" target="_blank" class="badge badge-primary">View API Documentation</a>
                                <code>/transaction-service/swagger-ui.html</code>
                            </div>
                        </div>

                        <div class="service">
                            <h2>Notification Service</h2>
                            <p>Handles notifications and messaging.</p>
                            <div class="access-path">
                               
                                <a href="/notification-service/swagger-ui.html" target="_blank" class="badge badge-primary">View API Documentation</a>
                                <code>/notification-service/swagger-ui.html</code>
                                
                            </div>
                        </div>

                        <div class="footer">
                            <p>SMMSB Microservices Platform &copy; 2024</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}
