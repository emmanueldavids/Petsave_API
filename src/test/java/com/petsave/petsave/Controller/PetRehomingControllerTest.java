package com.petsave.petsave.Controller;

import com.petsave.petsave.Entity.PetRehoming;
import com.petsave.petsave.Service.PetRehomingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PetRehomingControllerTest {

    @Test
    void getMyRehomingsRouteReturnsOk() throws Exception {
        PetRehomingService petRehomingService = Mockito.mock(PetRehomingService.class);
        when(petRehomingService.listMyRehomings()).thenReturn(List.of(new PetRehoming()));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PetRehomingController(petRehomingService)).build();

        mockMvc.perform(get("/api/rehomings/my"))
                .andExpect(status().isOk());
    }
}
