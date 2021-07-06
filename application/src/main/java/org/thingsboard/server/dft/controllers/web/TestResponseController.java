package org.thingsboard.server.dft.controllers.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class TestResponseController {

    @RequestMapping(value = "/test-utf8", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> responseUTF8() {
        return new ResponseEntity<>("Xin chào Việt Nam", HttpStatus.OK);
    }
}
