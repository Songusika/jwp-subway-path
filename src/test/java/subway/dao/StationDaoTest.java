package subway.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import subway.entity.Station;
import subway.fixture.StationFixture.GangnamStation;
import subway.fixture.StationFixture.JamsilStation;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(ReplaceUnderscores.class)
@JdbcTest
@Sql({"classpath:schema-test.sql"})
class StationDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private StationDao stationDao;

    @BeforeEach
    void setUp() {
        stationDao = new StationDao(jdbcTemplate);
    }

    @Test
    void 역_엔티티를_받아_역을_저장한다() {
        // given
        final Station station = JamsilStation.jamsilStation;

        // when
        final Long result = stationDao.insert(station);

        //then
        assertThat(result).isPositive();
    }

    @Test
    void 등록된_모든_역을_조회한다() {
        // given
        final Long id1 = stationDao.insert(JamsilStation.jamsilStation);
        final Long id2 = stationDao.insert(GangnamStation.gangnamStation);

        // when
        final List<Station> result = stationDao.findAll();

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(1).getId()).isEqualTo(id1),
                () -> assertThat(result.get(1).getName()).isEqualTo("잠실역"),
                () -> assertThat(result.get(0).getId()).isEqualTo(id2),
                () -> assertThat(result.get(0).getName()).isEqualTo("강남역")
        );

    }

    @Test
    void 등록된_역을_Id로_조회한다() {
        // given
        final Long id = stationDao.insert(JamsilStation.jamsilStation);

        // when
        final Station result = stationDao.findById(id);

        // then
        assertThat(result.getName()).isEqualTo("잠실역");
    }

    @Test
    void 등록된_역을_수정한다() {
        // given
        final Long id = stationDao.insert(GangnamStation.gangnamStation);
        final Station 선릉역 = Station.of(id, "선릉역");

        // when
        stationDao.update(선릉역);
        final Station result = stationDao.findById(id);

        // then
        assertThat(result.getName()).isEqualTo("선릉역");
    }

    @Test
    void 등록된_역을_삭제한다() {
        // given
        final Long id = stationDao.insert(GangnamStation.gangnamStation);

        // when
        stationDao.deleteById(id);

        // then
        assertThrows(
                DataAccessException.class, () -> stationDao.findById(id)
        );
    }

}