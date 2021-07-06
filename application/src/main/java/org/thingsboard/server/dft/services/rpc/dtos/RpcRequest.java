package org.thingsboard.server.dft.services.rpc.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class RpcRequest {
    String method;
    Map<String, Integer> params;
}
