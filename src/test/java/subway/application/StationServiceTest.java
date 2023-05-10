package subway.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import subway.dao.StationDao;
import subway.dto.StationResponse;
import subway.dto.StationSaveRequest;
import subway.entity.Station;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @InjectMocks
    private StationService stationService;

    @Mock
    private StationDao stationDao;

    @Test
    void saveRequest를_받아서_역을_저장한다() {
        // given
        StationSaveRequest request = new StationSaveRequest("잠실역");
        when(stationDao.insert(request.toEntity())).thenReturn(1L);

        // when, then
        assertThat(stationService.saveStation(request)).isEqualTo(1L);
    }

    @Test
    void id_를_받아_해당_역을_조회한다() {
        // given
        Long id = 1L;
        final Station response = Station.of(id, "잠실역");
        doReturn(response).when(stationDao).findById(id);

        // when
        final StationResponse result = stationService.findStationResponseById(id);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(StationResponse.of(response));
    }

    @Test
    void 전체_역을_조회한다() {
        // given
        final Station 잠실역 = Station.of(1L, "잠실역");
        final Station 강남역 = Station.of(2L, "강남역");
        doReturn(List.of(잠실역, 강남역)).when(stationDao).findAll();

        // when
        final List<StationResponse> allStationResponses = stationService.findAllStationResponses();

        // then
        assertThat(allStationResponses)
                .usingRecursiveComparison()
                .isEqualTo(List.of(
                        StationResponse.of(잠실역),
                        StationResponse.of(강남역)
                ));
    }

    @Test
    void id_saveRequest를_받아_역을_업데이트_한다() {
        // given
        final Long id = 1L;
        final StationSaveRequest request = new StationSaveRequest("자암실역");

        // when
        stationService.updateStation(id, request);

        // then
        verify(stationDao, times(1)).updateById(id, request.toEntity());
    }

    @Test
    void id를_받아_역을_삭제한다() {
        // given
        final Long id = 1L;

        // when
        stationService.deleteStationById(id);

        // then
        verify(stationDao, times(1)).deleteById(id);
    }
}
