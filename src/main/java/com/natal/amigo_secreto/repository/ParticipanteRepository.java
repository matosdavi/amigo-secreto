package com.natal.amigo_secreto.repository;

import com.natal.amigo_secreto.model.Participante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    Optional<Participante> findByToken(String token);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByAmigoIsNotNull();

    List<Participante> findAllByOrderByNomeAsc();
}
