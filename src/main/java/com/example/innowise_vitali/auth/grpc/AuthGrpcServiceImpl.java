package com.example.innowise_vitali.auth.grpc;

import com.example.innowise_vitali.auth.entity.User;
import com.example.innowise_vitali.auth.repository.TokenRepository;
import com.example.innowise_vitali.auth.repository.UserRepository;
import com.example.innowise_vitali.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AuthGrpcServiceImpl extends AuthGrpcServiceGrpc.AuthGrpcServiceImplBase {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Override
    public void validateToken(ValidateTokenRequest request,
                              StreamObserver<ValidateTokenResponse> observer) {

        ValidateTokenResponse.Builder resp = ValidateTokenResponse.newBuilder();

        try {
            String username = jwtService.extractUsername(request.getToken());
            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                observer.onNext(resp.setValid(false).build());
                observer.onCompleted();
                return;
            }

            boolean notRevoked = tokenRepository
                    .findByToken(request.getToken())
                    .map(t -> !t.isRevoked())
                    .orElse(false);

            if (!jwtService.isTokenValid(request.getToken(), user) || !notRevoked) {
                log.warn("gRPC: invalid/revoked token for {}", username);
                observer.onNext(resp.setValid(false).build());
                observer.onCompleted();
                return;
            }

            observer.onNext(resp
                    .setValid(true)
                    .setUserId(user.getId())
                    .setUsername(user.getUsername())
                    .setEmail(user.getEmail())
                    .setRole(user.getRole().name())
                    .build());

            log.info("gRPC: token valid for {}", username);

        } catch (Exception e) {
            log.error("gRPC validateToken error: {}", e.getMessage());
            observer.onNext(resp.setValid(false).build());
        }

        observer.onCompleted();
    }
}