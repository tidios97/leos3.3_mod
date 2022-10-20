package eu.europa.ec.leos.cmis.extensions;

import eu.europa.ec.leos.domain.cmis.LeosPackage;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CmisFolderExtensionsTest {

    private static final String FOLDER_ID = "FOLDER_ID";
    private static final String FOLDER_NAME = "FOLDER_NAME";
    private static final String FOLDER_PATH = "FOLDER_PATH";

    @Test
    public void test_toLeosPackage() {
        //setup
        Folder folder = mock(Folder.class);
        when(folder.getId()).thenReturn(FOLDER_ID);
        when(folder.getName()).thenReturn(FOLDER_NAME);
        when(folder.getPath()).thenReturn(FOLDER_PATH);

        //make call
        LeosPackage leosPackage = CmisFolderExtensions.toLeosPackage(folder);

        //verify
        assertThat(leosPackage, is(notNullValue()));
        assertThat(leosPackage.getId(), is(FOLDER_ID));
        assertThat(leosPackage.getName(), is(FOLDER_NAME));
        assertThat(leosPackage.getPath(), is(FOLDER_PATH));
    }
}