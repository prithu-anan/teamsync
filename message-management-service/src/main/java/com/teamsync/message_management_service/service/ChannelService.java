package com.teamsync.message_management_service.service;

import com.teamsync.message_management_service.dto.ChannelRequestDTO;
import com.teamsync.message_management_service.dto.ChannelResponseDTO;
import com.teamsync.message_management_service.dto.ChannelUpdateDTO;
import com.teamsync.message_management_service.entity.Channels;
// import com.teamsync.message_management_service.entity.Projects;
// import com.teamsync.message_management_service.entity.Users;
import com.teamsync.message_management_service.dto.ProjectDTO;
import com.teamsync.message_management_service.dto.UserResponseDTO;

import com.teamsync.message_management_service.exception.NotFoundException;
import com.teamsync.message_management_service.mapper.ChannelMapper;
import com.teamsync.message_management_service.repository.ChannelRepository;
// import com.teamsync.message_management_service.repository.ProjectRepository;
// import com.teamsync.message_management_service.repository.UserRepository;
import com.teamsync.message_management_service.client.ProjectClient;
import com.teamsync.message_management_service.client.UserClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChannelService {

    @Autowired
    private ChannelRepository channelRepository;

    // @Autowired
    // private UserRepository userRepository;

    @Autowired
    private UserClient userClient;

    // @Autowired
    // private ProjectRepository projectRepository;

    @Autowired
    private ProjectClient projectClient;

    @Autowired
    private ChannelMapper channelMapper;

    @Transactional
    public void createChannel(ChannelRequestDTO requestDto) {
        // Validate project existence
        ProjectDTO project = projectClient.findById(requestDto.projectId())
                .orElseThrow(() -> new NotFoundException("Project with ID " + requestDto.projectId() + " not found"));

        // Validate member existence
        List<Long> memberIds = requestDto.memberIds();
        for (Long memberId : memberIds) {
            if (!userClient.existsById(memberId)) {
                throw new NotFoundException("User with ID " + memberId + " not found");
            }
        }
        Channels channel = channelMapper.toEntity(requestDto);
        channel.setProject(project.getId());
        channelRepository.save(channel);
    }

    public ChannelResponseDTO getChannelById(Long id) {
        Channels channel = channelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Channel with ID " + id + " not found"));

        return channelMapper.toDto(channel);
    }

    public List<ChannelResponseDTO> getAllChannels() {
        List<Channels> channels = channelRepository.findAll();
        return channels.stream()
                .map(channelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateChannel(Long id, ChannelUpdateDTO dto) {
        Channels existing = channelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Channel not found: " + id));

        ProjectDTO project = projectClient.findById(dto.projectId())
                .orElseThrow(() -> new NotFoundException("Project not found: " + dto.projectId()));

        List<Long> memberIds = dto.members();
        for (Long memberId : memberIds) {
            UserResponseDTO user = userClient.findById(memberId)
                    .orElseThrow(() -> new NotFoundException("User with ID " + memberId + " not found"));
        }
        channelMapper.updateEntityFromDto(dto, existing);
        existing.setProject(project.getId());
        channelRepository.save(existing);
    }

    @Transactional
    public void deleteChannel(Long id) {
        if (!channelRepository.existsById(id)) {
            throw new NotFoundException("Channel with ID " + id + " not found");
        }
        channelRepository.deleteById(id);
    }
}