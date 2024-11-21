package com.cricket.service.implementation;

import com.cricket.dto.BaseResponseDTO;
import com.cricket.dto.MatchesDTO;
import com.cricket.dto.SeriesDTO;
import com.cricket.dto.TeamDTO;
import com.cricket.entity.Matches;
import com.cricket.entity.Series;
import com.cricket.repository.SeriesRepository;
import com.cricket.service.intrface.SeriesService;
import com.cricket.util.ApplicationConstants;
import com.cricket.util.Convertion;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeriesServiceImplementation implements SeriesService {

    @Autowired
    private Convertion convertion;

    @Autowired
    private SeriesRepository seriesRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public ResponseEntity<BaseResponseDTO> saveSeries(SeriesDTO seriesDTO) {
        BaseResponseDTO baseResponseDTO = new BaseResponseDTO();
        try {
            if (!ObjectUtils.isEmpty(seriesDTO) && !StringUtils.isEmpty(seriesDTO.getName()) && !StringUtils.isEmpty(seriesDTO.getFormat())) {
                seriesRepository.save(convertion.convertToEntity(seriesDTO, Series.class));
                baseResponseDTO.setMessage(ApplicationConstants.SERIES_SAVED_SUCCESS);
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            } else {
                baseResponseDTO.setMessage(ApplicationConstants.NULL_INPUT);
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            baseResponseDTO.setMessage(ApplicationConstants.ERROR_SAVING_SERIES);
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity<List<SeriesDTO>> getAllSeries() {
        SeriesDTO seriesDTO = new SeriesDTO();
        try {
            List<Series> seriesList = seriesRepository.findAllSeriesByOrder();
            if (!ObjectUtils.isEmpty(seriesList)) {
                List<SeriesDTO> fetchedSeriesDTO = seriesList.stream().map(series -> convertion.convertToDto(series, SeriesDTO.class)).toList();
                return new ResponseEntity<>(fetchedSeriesDTO, HttpStatus.OK);
            } else {
                seriesDTO.setMessage(ApplicationConstants.EMPTY_OUTPUT);
                return new ResponseEntity<>(List.of(seriesDTO), HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            seriesDTO.setMessage(ApplicationConstants.ERROR_FETCHING_SERIES);
            return new ResponseEntity<>(List.of(seriesDTO), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public ResponseEntity<SeriesDTO> getSeries(UUID id) {
        SeriesDTO seriesDTO = new SeriesDTO();
        try {
            if (!ObjectUtils.isEmpty(id)) {
                Series series = seriesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(ApplicationConstants.SERIES_NOT_FOUND));
                SeriesDTO fetchedSeriesDTO = convertion.convertToDto(series, SeriesDTO.class);
                fetchedSeriesDTO.setMatchesDTOList(series.getMatches().stream().map(matches -> convertion.convertToDto(matches, MatchesDTO.class)).toList());
                return new ResponseEntity<>(fetchedSeriesDTO, HttpStatus.OK);

            } else {
                seriesDTO.setMessage(ApplicationConstants.NULL_INPUT);
                return new ResponseEntity<>(seriesDTO, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            seriesDTO.setMessage(e.getLocalizedMessage());
            return new ResponseEntity<>(seriesDTO, HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity<List<MatchesDTO>> getMatchesBySeriesId(UUID seriesId) {
        MatchesDTO matchesDTO = new MatchesDTO();
        try {
            if (!ObjectUtils.isEmpty(seriesId)) {
                Series series = seriesRepository.findById(seriesId).orElseThrow(() -> new IllegalArgumentException(ApplicationConstants.SERIES_NOT_FOUND));
                List<Matches> listOfMatches = series.getMatches();
                if(!CollectionUtils.isEmpty(listOfMatches))
                {
                    List<MatchesDTO> matchesDTOList =  series.getMatches().stream().map(matches ->{
                            MatchesDTO belongingMatchesDTO=convertion.convertToDto(matches, MatchesDTO.class);
                            belongingMatchesDTO.setTeamsDTO(matches.getTeams().stream().map(team->convertion.convertToDto(team, TeamDTO.class)).collect(Collectors.toList()));
                            return  belongingMatchesDTO;
                    }).toList();

                    return new ResponseEntity<>(matchesDTOList, HttpStatus.OK);
                }
                else {
                    matchesDTO.setMessage(ApplicationConstants.EMPTY_OUTPUT);
                    return new ResponseEntity<>(List.of(matchesDTO), HttpStatus.NO_CONTENT);
                }

            } else {
                matchesDTO.setMessage(ApplicationConstants.NULL_INPUT);
                return new ResponseEntity<>(List.of(matchesDTO), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            matchesDTO.setMessage(e.getLocalizedMessage());
            return new ResponseEntity<>(List.of(matchesDTO), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<BaseResponseDTO> updateSeries(SeriesDTO seriesDTO) {
        BaseResponseDTO baseResponseDTO = new BaseResponseDTO();
        try {
            if (!ObjectUtils.isEmpty(seriesDTO) && !ObjectUtils.isEmpty(seriesDTO.getId())) {
                Series series = seriesRepository.findById(seriesDTO.getId()).orElseThrow(() -> new RuntimeException(ApplicationConstants.SERIES_NOT_FOUND));
                if (!ObjectUtils.isEmpty(seriesDTO)) {
                    Series updatedSeries = objectMapper.readerForUpdating(series).readValue(objectMapper.writeValueAsBytes(seriesDTO));
                    seriesRepository.save(updatedSeries);
                    baseResponseDTO.setMessage(ApplicationConstants.SERIES_UPDATED_SUCCESS);
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
                }
                baseResponseDTO.setMessage(ApplicationConstants.EMPTY_OUTPUT);
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NO_CONTENT);

            }
            baseResponseDTO.setMessage(ApplicationConstants.NULL_INPUT);
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            baseResponseDTO.setMessage(e.getLocalizedMessage());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

    }
}