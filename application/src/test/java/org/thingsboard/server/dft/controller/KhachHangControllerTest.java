package org.thingsboard.server.dft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.thingsboard.server.ThingsboardServerApplication;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.KhachHangDto;
import org.thingsboard.server.dft.services.khachhang.KhachHangService;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = ThingsboardServerApplication.class)
@AutoConfigureMockMvc(addFilters = false)
public class KhachHangControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private KhachHangService khachHangService;

  //  @MockBean private KhachHangController khachHangController;

  @LocalServerPort int randomServerPort;

  private ObjectMapper jsonMapper = new ObjectMapper();

  private KhachHangDto khachHangDto;

  @Before
  public void InitTest() throws Exception {
    khachHangDto = new KhachHangDto();
    khachHangDto.setMaKhachHang("KH1");
    khachHangDto.setTenKhachHang("Khach hang 1");
    khachHangDto.setEmail("email@gmail.com");
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testFindAllKhachHang_WithAllParam_ExpectReturnOk() throws Exception {
    PageData<KhachHangDto> pageData = new PageData<>(Collections.EMPTY_LIST, 1, 0, false);
    when(khachHangService.findAllByTenKhachHang(any(), any())).thenReturn(pageData);

    mockMvc
        .perform(
            get("/api/sys-admin/khach-hang")
                .param("page", "0")
                .param("pageSize", "10")
                .param("textSearch", "KH")
                .param("sortProperty", "id")
                .param("sortOrder", "DESC"))
        .andExpect(status().isOk());

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    ArgumentCaptor<String> searchTextCaptor = ArgumentCaptor.forClass(String.class);
    verify(khachHangService)
        .findAllByTenKhachHang(pageableCaptor.capture(), searchTextCaptor.capture());
    PageRequest pageable = (PageRequest) pageableCaptor.getValue();

    assertThat(pageable.getPageSize(), Matchers.equalTo(10));
    assertThat(pageable.getPageNumber(), Matchers.equalTo(0));
    assertThat(searchTextCaptor.getValue(), Matchers.equalTo("KH"));
    assertThat(pageable.getSort(), Matchers.equalTo(Sort.by("id").descending()));
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testFindAllKhachHang_WithNullRequireParam_ExpectReturnBadRequest() throws Exception {
    PageData<KhachHangDto> pageData = new PageData<>(Collections.EMPTY_LIST, 1, 0, false);
    when(khachHangService.findAllByTenKhachHang(any(), any())).thenReturn(pageData);

    mockMvc.perform(get("/api/sys-admin/khach-hang")).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testFindAllKhachHang_WithNullOptionParam_ExpectReturnOk() throws Exception {
    PageData<KhachHangDto> pageData = new PageData<>(Collections.EMPTY_LIST, 1, 0, false);
    when(khachHangService.findAllByTenKhachHang(any(), any())).thenReturn(pageData);
    mockMvc
        .perform(get("/api/sys-admin/khach-hang").param("page", "0").param("pageSize", "10"))
        .andExpect(status().isOk());

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(khachHangService).findAllByTenKhachHang(pageableCaptor.capture(), any());
    PageRequest pageable = (PageRequest) pageableCaptor.getValue();

    assertThat(pageable.getPageSize(), Matchers.equalTo(10));
    assertThat(pageable.getPageNumber(), Matchers.equalTo(0));
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testFindAllKhachHang_ExpertReturnPageData() throws Exception {
    khachHangDto.setId(UUID.randomUUID());
    PageData<KhachHangDto> pageData = new PageData<>(Arrays.asList(khachHangDto), 1, 1, false);
    when(khachHangService.findAllByTenKhachHang(any(), any())).thenReturn(pageData);

    mockMvc
        .perform(get("/api/sys-admin/khach-hang").param("page", "0").param("pageSize", "10"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonMapper.writeValueAsString(pageData)))
        .andReturn();
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testFindAllKhachHang_ReturnNull_ExpectBadRequest() throws Exception {
    when(khachHangService.findAllByTenKhachHang(any(), any())).thenReturn(null);

    mockMvc
        .perform(get("/api/sys-admin/khach-hang").param("page", "0").param("pageSize", "10"))
        .andExpect(status().isNotFound())
        .andReturn();
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testFindOne_WithMaKhachHang_ExpectReturnNotFound() throws Exception {
    UUID id = UUID.randomUUID();
    when(khachHangService.findKhachHangById(any())).thenReturn(null);

    mockMvc
        .perform(get("/api/sys-admin/khach-hang/" + id))
        .andExpect(status().isNotFound())
        .andReturn();

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(khachHangService).findKhachHangById(uuidCaptor.capture());

    assertThat(uuidCaptor.getValue(), Matchers.equalTo(id));
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testFindOne_WithUUID_ExpertReturnOneKhachHang() throws Exception {
    UUID id = UUID.randomUUID();
    khachHangDto.setId(id);
    when(khachHangService.findKhachHangById(any())).thenReturn(khachHangDto);

    mockMvc
        .perform(get("/api/sys-admin/khach-hang/" + id))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonMapper.writeValueAsString(khachHangDto)))
        .andReturn();

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(khachHangService).findKhachHangById(uuidCaptor.capture());

    assertThat(uuidCaptor.getValue(), Matchers.equalTo(id));
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testFindOne_WithParamNotUUID_ExpertReturnBadRequest() throws Exception {
    mockMvc
        .perform(get("/api/sys-admin/khach-hang/adsfasdfsadffasdf"))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testDelete_WithUUID_ExpertReturnOk() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/sys-admin/khach-hang/" + id))
        .andExpect(status().isOk())
        .andReturn();

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(khachHangService, atLeastOnce()).deleteById(uuidCaptor.capture());

    assertThat(uuidCaptor.getValue(), Matchers.equalTo(id));
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testDelete_WithParamNotUUID_ExpertReturnBadRequest() throws Exception {
    mockMvc
        .perform(delete("/api/sys-admin/khach-hang/aaaaa"))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testDelete_WithParamNull_ExpertReturnBadRequest() throws Exception {
    mockMvc
        .perform(delete("/api/sys-admin/khach-hang"))
        .andExpect(status().isMethodNotAllowed())
        .andReturn();
  }

  //  @Test
  //  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  //  public void testCreateOrUpdate_WithKhachHangDto_ExpertReturnKhachHangDto() throws Exception {
  //    UUID id = UUID.randomUUID();
  //    khachHangDto.setId(id.toString());
  //
  //    mockMvc
  //        .perform(
  //            post("/api/sys-admin/khach-hang")
  //                .content(
  //                    "{\n"
  //                        + "  \"email\": \"email@gmail.com\",\n"
  //                        + "  \"ghiChu\": \"string\",\n"
  //                        + "  \"maKhachHang\": \"KH1\",\n"
  //                        + "  \"soDienThoai\": \"0123123123\",\n"
  //                        + "  \"tenKhachHang\": \"Nguyen Van A\"\n"
  //                        + "}")
  //                .contentType("application/json"))
  //        .andExpect(status().isOk())
  //        .andReturn();
  //
  //    ResponseEntity responseEntity = new ResponseEntity(khachHangDto, HttpStatus.CREATED);
  //
  //    doReturn(responseEntity).when(khachHangController).saveKhachHang(any());
  //
  //    assertThat(khachHangDto.getMaKhachHang(), Matchers.equalTo(khachHangDto.getMaKhachHang()));
  //    assertThat(khachHangDto.getId(), Matchers.equalTo(id.toString()));
  //  }

  @Test
  @WithMockUser(username = "sysadmin", authorities = "SYS_ADMIN")
  public void testCreateOrUpdate_WithBodyNull_ExpertReturnBadRequest() throws Exception {
    mockMvc
        .perform(post("/api/sys-admin/khach-hang"))
        .andExpect(status().isBadRequest())
        .andReturn();
  }
}
