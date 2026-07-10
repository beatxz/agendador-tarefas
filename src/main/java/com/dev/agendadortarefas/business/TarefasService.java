package com.dev.agendadortarefas.business;

import com.dev.agendadortarefas.business.dto.TarefasDTO;
import com.dev.agendadortarefas.business.mapper.TarefaUpdateConverter;
import com.dev.agendadortarefas.business.mapper.TarefasConverter;
import com.dev.agendadortarefas.infrastructure.entity.TarefasEntity;
import com.dev.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import com.dev.agendadortarefas.infrastructure.exceptions.ResourceNotFoundException;
import com.dev.agendadortarefas.infrastructure.repository.TarefasRepository;
import com.dev.agendadortarefas.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefasService {

    private final TarefasRepository tarefasRepository;
    private final TarefasConverter tarefasConverter;
    private final JwtUtil jwtUtil;
    private final TarefaUpdateConverter tarefaUpdateConverter;

    public TarefasDTO gravarTarefa(String token ,TarefasDTO dto){
        String email = jwtUtil.extrairEmailToken((token.substring(7)));
        dto.setDataCriacao(LocalDateTime.now());
        dto.setStatusNotificacaoEnum(StatusNotificacaoEnum.PENDENTE);
        dto.setEmailUsuario(email);
        TarefasEntity entity = tarefasConverter.paraTarefaEntity(dto);
        return tarefasConverter.paraTarefaDTO(tarefasRepository.save(entity));
    }
    public List<TarefasDTO> buscaTerefasAgendadasPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal){
        return tarefasConverter.paraListaTarefasDTO(
                tarefasRepository.findByDataEventoBetweenAndStatusNotificacaoEnum(dataInicial,dataFinal,StatusNotificacaoEnum.PENDENTE));
    }
    public List<TarefasDTO> buscarTarefasPorEmail(String token){
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        List<TarefasEntity>listaTarefas = tarefasRepository.findByEmailUsuario(email);
        return tarefasConverter.paraListaTarefasDTO(listaTarefas);
    }
    public void deletaTarefaPorId(String id) {
        try {
            tarefasRepository.deleteById(id);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao deletar tarefa por id, id inexistente " + id, e.getCause());
        }
    }
    public TarefasDTO alterarStatus(StatusNotificacaoEnum status, String id) {
        try {
            TarefasEntity entity = tarefasRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada " + id));
            entity.setStatusNotificacaoEnum(status);
            return tarefasConverter.paraTarefaDTO(tarefasRepository.save(entity));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao encontrar tarefa " + e.getCause());
        }
    }
    public TarefasDTO updateTarefas(TarefasDTO dto, String id){
        try{
            TarefasEntity entity = tarefasRepository.findById(id)
                    .orElseThrow(()->new ResourceNotFoundException("Tarefa não encontrada "+id));
            tarefaUpdateConverter.updateTarefas(dto,entity);
            return tarefasConverter.paraTarefaDTO(tarefasRepository.save(entity));
        }catch(ResourceNotFoundException e){
            throw  new ResourceNotFoundException(("Erro ao atualizar a tarefa "+e.getCause()));
        }
    }
}
