package subway.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import subway.dao.LineDao;
import subway.dao.SectionDao;
import subway.dao.StationDao;
import subway.domain.line.Line;
import subway.domain.line.LineColor;
import subway.domain.line.LineName;
import subway.domain.section.Distance;
import subway.domain.section.Section;
import subway.domain.section.Sections;
import subway.domain.station.Station;
import subway.domain.station.StationName;
import subway.entity.StationEntity;
import subway.fixture.LineFixture.Line1;
import subway.fixture.LineFixture.Line2;
import subway.fixture.StationFixture.A;
import subway.fixture.StationFixture.B;
import subway.fixture.StationFixture.C;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(ReplaceUnderscores.class)
@JdbcTest
class LineRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private LineDao lineDao;
    private SectionDao sectionDao;
    private StationDao stationDao;
    private LineRepository lineRepository;

    @BeforeEach
    void setUp() {
        lineDao = new LineDao(jdbcTemplate);
        sectionDao = new SectionDao(jdbcTemplate);
        stationDao = new StationDao(jdbcTemplate);
        lineRepository = new LineRepository(lineDao, sectionDao, stationDao);
    }

    @Test
    void 노선을_저장한다() {
        // given
        final Line line = Line1.line;

        //when
        lineRepository.save(line);

        //then
        assertThat(lineDao.findByName("1호선").get().getName()).isEqualTo("1호선");
        ;
    }

    @Test
    void 노선에_속한_구간을_저장한다() {
        //given
        final Long lineId = lineDao.insert(Line1.entity);
        final Station stationA = getStationByStationEntity(A.entity);
        final Station stationB = getStationByStationEntity(B.entity);
        final Station stationC = getStationByStationEntity(C.entity);

        final Section section1 = new Section(stationA, stationB, new Distance(5));
        final Section section2 = new Section(stationB, stationC, new Distance(5));

        final Sections sections = new Sections(List.of(section1, section2));

        final Line line = new Line(lineId, Line1.name, Line1.color, sections);

        //when
        lineRepository.updateSections(line);

        //then
        assertThat(sectionDao.findAllByLineId(lineId)).hasSize(2);
    }

    private Station getStationByStationEntity(final StationEntity stationEntity) {
        final Long id = stationDao.insert(stationEntity);
        return new Station(id,
                new StationName(stationEntity.getName()));
    }

    @Test
    void 노선에_속한_구간을_삭제한다() {
        //given
        final Long lineId = lineDao.insert(Line1.entity);
        final Station stationA = getStationByStationEntity(A.entity);
        final Station stationB = getStationByStationEntity(B.entity);
        final Station stationC = getStationByStationEntity(C.entity);

        final Section section1 = new Section(stationA, stationB, new Distance(5));
        final Section section2 = new Section(stationB, stationC, new Distance(5));

        final Sections sections = new Sections(List.of(section1, section2));

        final Line line = new Line(lineId, Line1.name, Line1.color, sections);
        final Line removedLine = line.deleteStation(stationA);

        //when
        lineRepository.updateSections(removedLine);

        //then
        assertThat(sectionDao.findAllByLineId(lineId)).hasSize(1);
    }

    @Test
    void 노선을_노선이름으로_조회한다() {
        // given
        lineDao.insert(Line1.entity);

        // expect
        assertThat(lineRepository.findByName(new LineName("1호선")).get().getLineName().name())
                .isEqualTo("1호선");
    }

    @Test
    void 모든_노선을_찾는다() {
        // given
        lineDao.insert(Line1.entity);
        lineDao.insert(Line2.entity);

        // expect
        List<Line> lines = lineRepository.findAllLine();
        assertAll(
                () -> assertThat(lines)
                        .hasSize(2),
                () -> assertThat(lines.get(0).getLineName().name())
                        .isEqualTo("1호선"),
                () -> assertThat(lines.get(1).getLineName().name())
                        .isEqualTo("2호선")
        );
    }

    @Test
    void 기존_노선을_업데이트_한다() {
        // given
        final Long id = lineDao.insert(Line1.entity);

        // when
        lineRepository.update(new Line(id, new LineName("1-호선"), new LineColor("검정")));

        //then
        assertAll(
                () -> assertThat(lineDao.findById(id).get().getName()).isEqualTo("1-호선"),
                () -> assertThat(lineDao.findById(id).get().getColor()).isEqualTo("검정"),
                () -> assertThat(lineDao.findById(id).get().getId()).isEqualTo(id)
        );
    }

    @Test
    void 노선을_삭제한다() {
        // given
        final Long id = lineDao.insert(Line1.entity);

        // when
        lineRepository.delete(new Line(id, Line1.name, Line1.color));

        // then
        assertThatThrownBy(() -> lineRepository.findByName(new LineName("1호선")).get())
                .isInstanceOf(NoSuchElementException.class);
    }
}