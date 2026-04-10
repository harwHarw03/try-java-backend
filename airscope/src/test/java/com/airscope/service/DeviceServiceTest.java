package com.airscope.service;

import com.airscope.dto.DeviceDto.DeviceRequest;
import com.airscope.dto.DeviceDto.DeviceResponse;
import com.airscope.model.Device;
import com.airscope.model.User;
import com.airscope.repository.DeviceRepository;
import com.airscope.repository.UserRepository;
import com.airscope.util.AppExceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeviceService deviceService;

    private User testUser;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        testDevice = Device.builder()
                .id(1L)
                .name("Test Device")
                .user(testUser)
                .build();
    }

    @Test
    void createDevice_Success() {
        DeviceRequest request = new DeviceRequest();
        request.setName("New Device");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device device = invocation.getArgument(0);
            device.setId(1L);
            return device;
        });

        DeviceResponse response = deviceService.createDevice(request, "test@example.com");

        assertNotNull(response);
        assertEquals("New Device", response.getName());
        assertEquals(1L, response.getUserId());
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void createDevice_UserNotFound() {
        DeviceRequest request = new DeviceRequest();
        request.setName("New Device");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(AppExceptions.ResourceNotFoundException.class, () ->
                deviceService.createDevice(request, "unknown@example.com"));
    }

    @Test
    void getUserDevices_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(deviceRepository.findByUserId(1L)).thenReturn(List.of(testDevice));

        List<DeviceResponse> devices = deviceService.getUserDevices("test@example.com");

        assertEquals(1, devices.size());
        assertEquals("Test Device", devices.get(0).getName());
    }

    @Test
    void updateDevice_Success() {
        DeviceRequest request = new DeviceRequest();
        request.setName("Updated Device");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(deviceRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        DeviceResponse response = deviceService.updateDevice(1L, request, "test@example.com");

        assertNotNull(response);
        assertEquals("Updated Device", testDevice.getName());
    }

    @Test
    void updateDevice_Unauthorized() {
        DeviceRequest request = new DeviceRequest();
        request.setName("Updated Device");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(deviceRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(AppExceptions.UnauthorizedException.class, () ->
                deviceService.updateDevice(1L, request, "test@example.com"));
    }

    @Test
    void deleteDevice_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(deviceRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testDevice));
        doNothing().when(deviceRepository).delete(testDevice);

        assertDoesNotThrow(() -> deviceService.deleteDevice(1L, "test@example.com"));
        verify(deviceRepository).delete(testDevice);
    }
}
