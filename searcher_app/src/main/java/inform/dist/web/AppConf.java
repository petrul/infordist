package inform.dist.web;

import inform.dist.ngd.DistanceCalculatorFromFreqMatrix;
import inform.dist.web.beans.TermService;
import matrix.store.TermMatrixReadOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;


@Configuration
public class AppConf {

    @Bean
    public TermMatrixReadOnly getTermMatrix(@Value("${ngd.matrix.location}") String ngdMatrixLocation) {
        String tildeResolved = Util.replaceTilde(ngdMatrixLocation);
        return new TermMatrixReadOnly(new File(tildeResolved));
    }

    @Bean
    public TermService getTermService(TermMatrixReadOnly termMatrix) {
        return new TermService(termMatrix);
    }

    @Bean
    public DistanceCalculatorFromFreqMatrix getDistCalculator(TermMatrixReadOnly termMatrix) {
        return new DistanceCalculatorFromFreqMatrix(termMatrix);
    }

    @Bean
    public NgdNeighboursService getNgdNeighboursService(TermService termService) {
        return new NgdNeighboursService(termService);
    }

}
