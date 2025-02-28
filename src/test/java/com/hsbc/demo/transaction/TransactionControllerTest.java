package com.hsbc.demo.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.hsbc.demo.transaction.models.common.ApiErrorCode.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 指定按 @Order 注解排序
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    public void testQueryWithNullId() throws Exception {

        Long[] ids = new Long[101];
        ids[0] = 0L;
        for (int i = 0; i < 100; i++) {
            Long id = insert("aa" + i);
            ids[i + 1] = id;
        }

        for (int i = 0; i < 100; i += 10) {
            String url = "/api/transaction/query?pageSize=10";
            if (i != 0) {
                url += "&lastTransactionId=" + ids[i];
            }
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.data").isArray())
                    .andExpect(jsonPath("$.data.data[0].id").value(ids[i + 1]))
                    .andExpect(jsonPath("$.data.data[1].id").value(ids[i + 2]))
                    .andExpect(jsonPath("$.data.data[2].id").value(ids[i + 3]))
                    .andExpect(jsonPath("$.data.data[3].id").value(ids[i + 4]))
                    .andExpect(jsonPath("$.data.data[4].id").value(ids[i + 5]))
                    .andExpect(jsonPath("$.data.data[5].id").value(ids[i + 6]))
                    .andExpect(jsonPath("$.data.data[6].id").value(ids[i + 7]))
                    .andExpect(jsonPath("$.data.data[7].id").value(ids[i + 8]))
                    .andExpect(jsonPath("$.data.data[8].id").value(ids[i + 9]))
                    .andExpect(jsonPath("$.data.data[9].id").value(ids[i + 10]))
            ;

        }

    }

    private ResultActions confirmInvalidParam(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().is(500))
                .andExpect(jsonPath("$.code").value(ERR_INVALID_PARAMS.getCode()));
    }


    private Long insert(String appendix) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/transaction/create")
                        .content("""
                                {
                                    "clientTransactionId": "clientTransactionId<appendix>",
                                    "fromAccountId": "fromAccountId<appendix>",
                                    "toAccountId": "toAccountId<appendix>",
                                    "amount": 100,
                                    "currency": "USD",
                                    "remark": "remark<appendix>"
                                }""".replaceAll("<appendix>", appendix))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.clientTransactionId").value("clientTransactionId" + appendix))
                .andExpect(jsonPath("$.data.fromAccountId").value("fromAccountId" + appendix))
                .andExpect(jsonPath("$.data.toAccountId").value("toAccountId" + appendix))
                .andExpect(jsonPath("$.data.amount").value(100))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.remark").value("remark" + appendix))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String idString = JsonPath.read(responseBody, "$.data.id").toString();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transaction/getById?id=" + idString))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.clientTransactionId").value("clientTransactionId" + appendix))
                .andExpect(jsonPath("$.data.fromAccountId").value("fromAccountId" + appendix))
                .andExpect(jsonPath("$.data.toAccountId").value("toAccountId" + appendix))
                .andExpect(jsonPath("$.data.amount").value(100))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.remark").value("remark" + appendix));
        return Long.parseLong(idString);
    }

    void testGetByIdSuccess(long id) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transaction/getById?id=" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    void testGetByIdNotExists(long id) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transaction/getById?id=" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.code").value(ERR_INVALID_TRANSACTION_ID.getCode()));
    }

    @Test
    public void testCreateSuccess() throws Exception {
        Long id = insert("111");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/transaction/delete?id=" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.code").value(0))
        ;
        testGetByIdNotExists(id);
    }

    @Test
    public void testCreateTwice() throws Exception {
        insert("222");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/transaction/create")
                        .content("""
                                {
                                    "clientTransactionId": "clientTransactionId222",
                                    "fromAccountId": "fromAccountId222",
                                    "toAccountId": "toAccountId222",
                                    "amount": 100,
                                    "currency": "USD",
                                    "remark": "remark222"
                                }""")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.code").value(ERR_TRANSACTION_EXISTS.getCode()));
    }

    @Test
    public void testCreateInvalidArgs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transaction/create")
                        .content("""
                                {
                                    "clientTransactionId": "clientTransactionId333"
                                }""")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.code").value(ERR_INVALID_PARAMS.getCode()));
    }


    @Test
    public void testDeleteSuccess() throws Exception {
        Long id = insert("444");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/transaction/delete?id=" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.code").value(SUCCESS.getCode()));

        testGetByIdNotExists(id);
    }


    @Test
    public void testDeleteInvalidId() throws Exception {
        testGetByIdNotExists(12312312312L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/transaction/delete?id=12312312312")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.code").value(ERR_INVALID_TRANSACTION_ID.getCode()));
    }


    @Test
    public void testDeleteInvalidIdFormat() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/transaction/delete?id=kkkkkk")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.code").value(ERR_COMMON.getCode()));
    }


    @Test
    public void testUpdateSuccess() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/transaction/create")
                        .content("""
                                {
                                    "clientTransactionId": "clientTransactionId555",
                                    "fromAccountId": "fromAccountId555",
                                    "toAccountId": "toAccountId555",
                                    "amount": 100,
                                    "currency": "USD",
                                    "remark": "remark555"
                                }""")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.clientTransactionId").value("clientTransactionId555"))
                .andExpect(jsonPath("$.data.fromAccountId").value("fromAccountId555"))
                .andExpect(jsonPath("$.data.toAccountId").value("toAccountId555"))
                .andExpect(jsonPath("$.data.amount").value(100))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.remark").value("remark555"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Long id = JsonPath.read(responseBody, "$.data.id");
        String versionStr = JsonPath.read(responseBody, "$.data.version").toString();
        mockMvc.perform(MockMvcRequestBuilders.put("/api/transaction/modify")
                        .content("""
                                {
                                    "transactionId":"<id>",
                                    "remark": "remark666",
                                    "status":2,
                                    "version": <version>
                                }""".replace("<id>", id.toString()).replace("<version>", versionStr))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/transaction/getById?id=" + id))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.remark").value("remark666"))
                .andExpect(jsonPath("$.data.status").value(2))
        ;
    }


    @Test
    public void testUpdateWithInvalidVersion() throws Exception {
        Long id = insert("777");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/transaction/modify")
                        .content("""
                                {
                                    "transactionId":"<id>",
                                    "remark": "remark666",
                                    "status":2,
                                    "version": 1555
                                }""".replace("<id>", id.toString()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.code").value(ERR_DATA_HAS_BEEN_MODIFIED.getCode()));


    }

    @Test
    public void testUpdateInvalidId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/transaction/modify")
                        .content("""
                                {
                                    "transactionId":"1991",
                                    "remark": "remark666",
                                    "status":2,
                                    "version": 1
                                }""")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.code").value(ERR_INVALID_TRANSACTION_ID.getCode()));
    }

    @Test
    public void testQueryWithInvalidPageSize() throws Exception {
        ResultActions action = mockMvc.perform(MockMvcRequestBuilders.get("/api/transaction/query?pageSize=100000")
                .contentType(MediaType.APPLICATION_JSON));
        confirmInvalidParam(action);
    }


    @Test
    @Order(2)
    public void testQuerySuccess() throws Exception {
        Long lastId = insert("adfasdsd");

        Long[] ids = new Long[101];
        ids[0] = lastId;
        for (int i = 0; i < 100; i++) {
            Long id = insert("acca" + i);
            ids[i + 1] = id;
        }

        for (int i = 0; i < 100; i += 10) {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/transaction/query?pageSize=10&lastTransactionId=" + ids[i]))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.data").isArray())
                    .andExpect(jsonPath("$.data.data[0].id").value(ids[i + 1]))
                    .andExpect(jsonPath("$.data.data[1].id").value(ids[i + 2]))
                    .andExpect(jsonPath("$.data.data[2].id").value(ids[i + 3]))
                    .andExpect(jsonPath("$.data.data[3].id").value(ids[i + 4]))
                    .andExpect(jsonPath("$.data.data[4].id").value(ids[i + 5]))
                    .andExpect(jsonPath("$.data.data[5].id").value(ids[i + 6]))
                    .andExpect(jsonPath("$.data.data[6].id").value(ids[i + 7]))
                    .andExpect(jsonPath("$.data.data[7].id").value(ids[i + 8]))
                    .andExpect(jsonPath("$.data.data[8].id").value(ids[i + 9]))
                    .andExpect(jsonPath("$.data.data[9].id").value(ids[i + 10]))
            ;

        }

    }


}