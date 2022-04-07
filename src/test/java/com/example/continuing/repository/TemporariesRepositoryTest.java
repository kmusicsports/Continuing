package com.example.continuing.repository;

import com.example.continuing.common.Utils;
import com.example.continuing.entity.Temporaries;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TemporariesRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TemporariesRepository temporariesRepository;

    @Test
    public void testFindByEmailOrderByCreatedAtDesc() {
        String testEmail = "test@email";

        Date date = new Date();
        Timestamp timestampMinus30 = new Timestamp(date.getTime() - 1800000); // 30分前
        Timestamp timestampPlus30 = new Timestamp(date.getTime() + 1800000); // 30分後
        Timestamp timestampNow = Utils.timestampNow();

        Temporaries temporaries1 = new Temporaries();
        temporaries1.setName("testName1");
        temporaries1.setEmail(testEmail);
        temporaries1.setPassword("testPassword1");
        temporaries1.setToken("testToken1");
        temporaries1.setCreatedAt(timestampNow);
        
        Temporaries temporaries2 = new Temporaries();
        temporaries2.setName("testName2");
        temporaries2.setEmail("test2@email");
        temporaries2.setPassword("testPassword2");
        temporaries2.setToken("testToken2");
        temporaries2.setCreatedAt(timestampMinus30);

        Temporaries temporaries3 = new Temporaries();
        temporaries3.setName("testName3");
        temporaries3.setEmail(testEmail);
        temporaries3.setPassword("testPassword3");
        temporaries3.setToken("testToken3");
        temporaries3.setCreatedAt(timestampPlus30);

        entityManager.persist(temporaries1);
        entityManager.persist(temporaries2);
        entityManager.persist(temporaries3);

        List<Temporaries> actualList =  temporariesRepository.findByEmailOrderByCreatedAtDesc(testEmail);
        assertThat(actualList.get(0)).isEqualTo(temporaries3);
        assertThat(actualList.get(1)).isEqualTo(temporaries1);
        assertThat(actualList.size()).isEqualTo(2);

        entityManager.remove(temporaries1);
        entityManager.remove(temporaries2);
        entityManager.remove(temporaries3);
    }
}