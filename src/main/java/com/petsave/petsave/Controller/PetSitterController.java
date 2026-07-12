package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.PaystackTransferService;
import com.petsave.petsave.Service.PetSitterService;
import com.petsave.petsave.dto.PetSitterRequest;
import com.petsave.petsave.dto.PetSitterResponse;
import com.petsave.petsave.dto.SitterWalletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sitters")
@RequiredArgsConstructor
public class PetSitterController {

    private final PetSitterService petSitterService;
    private final PaystackTransferService paystackTransferService;

    @GetMapping
    public ResponseEntity<List<PetSitterResponse>> listActiveSitters(@RequestParam(required = false) String city) {
        return ResponseEntity.ok(petSitterService.listActiveSitters(city));
    }

    @GetMapping("/banks")
    public ResponseEntity<List<Map<String, Object>>> listBanks(@RequestParam(defaultValue = "NGN") String currency) {
        return ResponseEntity.ok(paystackTransferService.listBanks(currency));
    }

    @GetMapping("/my")
    public ResponseEntity<PetSitterResponse> getMySitterProfile() {
        return ResponseEntity.ok(petSitterService.getMySitterProfile());
    }

    @GetMapping("/my/wallet")
    public ResponseEntity<SitterWalletResponse> getMyWallet() {
        return ResponseEntity.ok(petSitterService.getMyWallet());
    }

    @GetMapping("/admin")
    public ResponseEntity<List<PetSitterResponse>> listForAdmin(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(petSitterService.listForAdmin(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetSitterResponse> getSitter(@PathVariable Long id) {
        return ResponseEntity.ok(petSitterService.getSitter(id));
    }

    @PostMapping
    public ResponseEntity<PetSitterResponse> registerSitter(@Valid @RequestBody PetSitterRequest request) {
        return ResponseEntity.ok(petSitterService.registerSitter(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetSitterResponse> updateSitterProfile(@PathVariable Long id, @Valid @RequestBody PetSitterRequest request) {
        return ResponseEntity.ok(petSitterService.updateSitterProfile(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PetSitterResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(petSitterService.updateStatus(id, body.get("status")));
    }
}
