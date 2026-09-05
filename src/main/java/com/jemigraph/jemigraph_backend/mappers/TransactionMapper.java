package com.jemigraph.jemigraph_backend.mappers;


import com.jemigraph.jemigraph_backend.DTO.TransactionAdminDTO;
import com.jemigraph.jemigraph_backend.Entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "userName", source = "wallet.user.name")
    @Mapping(target = "type", source = "direction")
    TransactionAdminDTO toTransactionAdminDTO(Transaction transaction);
}
