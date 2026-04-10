package com.airscope.controller;

import com.airscope.dto.DeviceDto.DeviceRequest;
import com.airscope.dto.DeviceDto.DeviceResponse;
import com.airscope.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "aws.dynamodb.local=false"
})
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceService deviceService;

    @MockBean
    private DynamoDbClient dynamoDbClient;

    private DeviceResponse deviceResponse;

    @BeforeEach
    void setUp() {
        deviceResponse = DeviceResponse.builder()
                .id(1L)
                .name("Test Device")
                .userId(1L)
                .build();
    }

    @Test
    void getDevices_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getDevices_Success() throws Exception {
        when(deviceService.getUserDevices("test@example.com"))
                .thenReturn(List.of(deviceResponse));

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Device"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createDevice_Success() throws Exception {
        DeviceRequest request = new DeviceRequest();
        request.setName("New Device");

        when(deviceService.createDevice(any(DeviceRequest.class), eq("test@example.com")))
                .thenReturn(deviceResponse);

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createDevice_ValidationError_EmptyName() throws Exception {
        DeviceRequest request = new DeviceRequest();
        request.setName("");

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateDevice_Success() throws Exception {
        DeviceRequest request = new DeviceRequest();
        request.setName("Updated Device");

        DeviceResponse updatedResponse = DeviceResponse.builder()
                .id(1L)
                .name("Updated Device")
                .userId(1L)
                .build();

        when(deviceService.updateDevice(eq(1L), any(DeviceRequest.class), eq("test@example.com")))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Device"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteDevice_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/devices/1"))
                .andExpect(status().isNoContent());
    }
}
