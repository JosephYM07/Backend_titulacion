package com.tesis.tigmotors.repository;

import com.tesis.tigmotors.models.Factura;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends MongoRepository<Factura, String> {
}