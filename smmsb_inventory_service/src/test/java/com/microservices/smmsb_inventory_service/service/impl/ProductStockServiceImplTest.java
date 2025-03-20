// package com.microservices.smmsb_inventory_service.service.impl;

// import com.microservices.smmsb_inventory_service.dto.ProductStockDto;
// import com.microservices.smmsb_inventory_service.dto.kafkaEvent.LowStockAlertEvent;
// import com.microservices.smmsb_inventory_service.dto.kafkaEvent.NotificationEvent;
// import com.microservices.smmsb_inventory_service.dto.request.CreateProductStockRequest;
// import com.microservices.smmsb_inventory_service.dto.request.UpdateProductStockRequest;
// import com.microservices.smmsb_inventory_service.dto.response.ApiDataResponseBuilder;
// import com.microservices.smmsb_inventory_service.dto.response.ListResponse;
// import com.microservices.smmsb_inventory_service.dto.response.MessageResponse;
// import com.microservices.smmsb_inventory_service.model.ProductStock;
// import com.microservices.smmsb_inventory_service.repository.ProductStockRepository;
// import com.microservices.smmsb_inventory_service.service.MinioService;
// import com.microservices.smmsb_inventory_service.service.kafka.KafkaProducer;
// import com.microservices.smmsb_inventory_service.utils.MessageUtils;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.modelmapper.ModelMapper;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.http.HttpStatus;
// import org.springframework.mock.web.MockHttpServletRequest;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.web.multipart.MultipartFile;

// import java.math.BigDecimal;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// public class ProductStockServiceImplTest {

//     @Mock
//     private ProductStockRepository productStockRepository;

//     @Mock
//     private ModelMapper modelMapper;

//     @Mock
//     private MessageUtils messageUtils;

//     @Mock
//     private MinioService minioService;

//     @Mock
//     private KafkaProducer kafkaProducer;

//     @InjectMocks
//     private ProductStockServiceImpl productStockService;

//     private MockHttpServletRequest request;
//     private ProductStock productStock;
//     private ProductStockDto productStockDto;
//     private CreateProductStockRequest createRequest;
//     private UpdateProductStockRequest updateRequest;

//     @BeforeEach
//     void setUp() {
//         request = new MockHttpServletRequest();
//         request.addHeader("X-User-Id", "1");

//         productStock = new ProductStock();
//         productStock.setId(1L);
//         productStock.setProductName("Test Product");
//         productStock.setQuantity(20);
//         productStock.setPrice(new BigDecimal("100000"));
//         productStock.setImageUrl("http://example.com/image.jpg");

//         productStockDto = new ProductStockDto();
//         productStockDto.setId(1);
//         productStockDto.setProductName("Test Product");
//         productStockDto.setQuantity(20);
//         productStockDto.setPrice(new BigDecimal("100000"));
//         productStockDto.setImageUrl("http://example.com/image.jpg");

//         createRequest = new CreateProductStockRequest();
//         createRequest.setProductName("Test Product");
//         createRequest.setQuantity(20);
//         createRequest.setPrice(new BigDecimal("100000"));

//         updateRequest = new UpdateProductStockRequest();
//         updateRequest.setProductName("Updated Product");
//         updateRequest.setQuantity(30);
//         updateRequest.setPrice(new BigDecimal("120000"));

//     }

//     @Test
//     void createProductStock_Success() {
//         when(modelMapper.map(any(CreateProductStockRequest.class), eq(ProductStock.class))).thenReturn(productStock);
//         when(productStockRepository.save(any(ProductStock.class))).thenReturn(productStock);
//         when(messageUtils.getMessage(eq("product.created"), anyString())).thenReturn("Product created successfully");

//         MessageResponse response = productStockService.createProductStock(createRequest, request);

//         assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
//         verify(kafkaProducer).sendNotificationEvent(any(NotificationEvent.class));
//         verify(productStockRepository).save(any(ProductStock.class));
//     }

//     @Test
//     void createProductStock_NoUserId() {
//         MockHttpServletRequest requestWithoutUserId = new MockHttpServletRequest();
//         when(messageUtils.getMessage(eq("user.not.found"))).thenReturn("User not found");

//         MessageResponse response = productStockService.createProductStock(createRequest, requestWithoutUserId);

//         assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());
//         verify(productStockRepository, never()).save(any(ProductStock.class));
//     }

//     @Test
//     void updateProductStock_Success() {
//         when(productStockRepository.findById(1L)).thenReturn(Optional.of(productStock));
//         when(messageUtils.getMessage(eq("product.updated"), anyString())).thenReturn("Product updated successfully");

//         MessageResponse response = productStockService.updateProductStock(1L, updateRequest, request);

//         assertEquals(HttpStatus.OK.value(), response.getStatusCode());
//         verify(modelMapper).map(updateRequest, productStock);
//         verify(productStockRepository).save(productStock);
//         verify(kafkaProducer).sendNotificationEvent(any(NotificationEvent.class));
//     }

//     @Test
//     void getProductStockById_Success() {
//         when(productStockRepository.findById(1L)).thenReturn(Optional.of(productStock));
//         when(modelMapper.map(productStock, ProductStockDto.class)).thenReturn(productStockDto);
//         when(messageUtils.getMessage(eq("products.found"), anyString())).thenReturn("Product found");

//         ApiDataResponseBuilder response = productStockService.getProductStockById(1L);

//         assertEquals(HttpStatus.OK.value(), response.getStatusCode());
//         assertEquals(productStockDto, response.getData());
//     }

//     @Test
//     void getProductStockById_NotFound() {
//         when(productStockRepository.findById(99L)).thenThrow(new IllegalArgumentException("Product not found with id: 99"));

//         assertThrows(IllegalArgumentException.class, () -> productStockService.getProductStockById(99L));
//     }

//     @Test
//     void deleteProductStock_Success() {
//         when(productStockRepository.findById(1L)).thenReturn(Optional.of(productStock));
//         when(messageUtils.getMessage(eq("product.deleted"), anyString())).thenReturn("Product deleted successfully");

//         MessageResponse response = productStockService.deleteProductStock(1L, request);

//         assertEquals(HttpStatus.OK.value(), response.getStatusCode());
//         verify(productStockRepository).delete(productStock);
//         verify(kafkaProducer).sendNotificationEvent(any(NotificationEvent.class));
//     }

//     @Test
//     void uploadImage_Success() {
//         MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
//         when(productStockRepository.findById(1L)).thenReturn(Optional.of(productStock));
//         when(minioService.uploadFile(any(MultipartFile.class), anyString(), eq("smmsbproducts"))).thenReturn("http://example.com/new-image.jpg");
//         when(messageUtils.getMessage(eq("productStock.uploadPhoto.success"), anyString())).thenReturn("Image uploaded successfully");

//         ListResponse<Map<String, String>> response = productStockService.uploadImage(file, 1L);

//         assertEquals(HttpStatus.OK.value(), response.getStatusCode());
//         assertEquals(1, response.getData().size());
//         assertEquals("http://example.com/new-image.jpg", response.getData().get(0).get("url"));
//         verify(productStockRepository).save(productStock);
//     }

//     @SuppressWarnings("unchecked")
//    @Test
//     void getAllProductStocks_Success() {
//         List<ProductStock> productStocks = new ArrayList<>();
//         productStocks.add(productStock);
//         Page<ProductStock> page = new PageImpl<>(productStocks);
        
//         when(productStockRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
//         when(modelMapper.map(any(ProductStock.class), eq(ProductStockDto.class))).thenReturn(productStockDto);
//         when(messageUtils.getMessage(eq("products.found"))).thenReturn("Products found");

//         Pageable pageable = PageRequest.of(0, 10);
//         ListResponse<ProductStockDto> response = productStockService.getAllProductStocks(pageable, "Test", new BigDecimal("100000.00"));

//         assertEquals(HttpStatus.OK.value(), response.getStatusCode());
//         assertEquals(1, response.getData().size());
//         assertEquals(productStockDto, response.getData().get(0));
//     }

//     @Test
//     void checkAndNotifyLowStockProducts() {
//         List<ProductStock> lowStockProducts = new ArrayList<>();
//         ProductStock lowStockProduct = new ProductStock();
//         lowStockProduct.setId(2L);
//         lowStockProduct.setProductName("Low Stock Product");
//         lowStockProduct.setQuantity(5);
//         lowStockProducts.add(lowStockProduct);

//         when(productStockRepository.findByQuantityLessThan(10)).thenReturn(lowStockProducts);

//         productStockService.checkAndNotifyLowStockProducts();

//         ArgumentCaptor<LowStockAlertEvent> lowStockCaptor = ArgumentCaptor.forClass(LowStockAlertEvent.class);
//         ArgumentCaptor<NotificationEvent> notificationCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        
//         verify(kafkaProducer).sendLowStockAlertEvent(lowStockCaptor.capture());
//         verify(kafkaProducer).sendNotificationEvent(notificationCaptor.capture());
        
//         assertEquals(2L, lowStockCaptor.getValue().getProductId());
//         assertEquals("LOW_STOCK_ALERT", notificationCaptor.getValue().getType());
//     }
// }
