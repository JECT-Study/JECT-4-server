package com.ject.studytrip.sample;

import com.ject.studytrip.global.common.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "sample", description = "샘플 API")
@RequestMapping("/api/sample")
@RestController
public class SampleController {

    @Operation(summary = "sampleGet", description = "GET 샘플 API 입니다.")
    @GetMapping()
    public ResponseEntity<StandardResponse> sampleGet(@RequestParam Long sampleId) {
        StandardResponse response = StandardResponse.success(HttpStatus.OK.value(), sampleId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "samplePost", description = "POST 샘플 API 입니다.")
    @PostMapping()
    public ResponseEntity<StandardResponse> samplePost(@RequestBody @Valid SampleRequest request) {
        SampleResponse sampleResponse = SampleResponse.of(request.sample(), request.sampleNum());
        StandardResponse response =
                StandardResponse.success(HttpStatus.CREATED.value(), sampleResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "samplePut", description = "PUT 샘플 API 입니다.")
    @PutMapping("/{sampleId}")
    public ResponseEntity<StandardResponse> samplePut(@PathVariable Long sampleId) {
        StandardResponse response = StandardResponse.success(HttpStatus.OK.value(), null);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "samplePatch", description = "PATCH 샘플 API 입니다.")
    @PatchMapping("/{sampleId}")
    public ResponseEntity<StandardResponse> samplePatch(@PathVariable Long sampleId) {
        StandardResponse response = StandardResponse.success(HttpStatus.OK.value(), null);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "sampleDelete", description = "DELETE 샘플 API 입니다.")
    @DeleteMapping("/{sampleId}")
    public ResponseEntity<StandardResponse> sampleDelete(@PathVariable Long sampleId) {
        StandardResponse response = StandardResponse.success(HttpStatus.OK.value(), sampleId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
