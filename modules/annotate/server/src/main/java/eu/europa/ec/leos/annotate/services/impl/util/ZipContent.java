/*
 * Copyright 2022 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.annotate.services.impl.util;

import eu.europa.ec.leos.annotate.Generated;
import org.apache.commons.io.FilenameUtils;

import java.util.Objects;

/**
 * Class represents the content data of a zip file.
 * */
public class ZipContent {
    
    private final String fullName;
    private byte[] data;

    /**
     * Constructor
     * @param fullName Name of the file including path (within zip)
     * @param data Data of the content as Object. Is {@literal null} for directories.
     * */
    public ZipContent(final String fullName, final byte[] data) {
        this.fullName = fullName;
        this.data = data;
    }

    /**
     * Returns the full content name including path within the zip file
     * @return Name of the content
     * */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the filename excluding path and file extension
     * @return Filename excluding path and file extension
     * */
    public String getName() {
        return FilenameUtils.getBaseName(fullName);
    }

    /**
     * Returns the filename including file extension
     * @return Filename including file extension
     * */
    public String getFilename() {
        return FilenameUtils.getName(fullName);
    }

    /**
     * Returns the path excluding filename
     * @return Full name without filename
     * */
    public String getPath() {
        return FilenameUtils.getPath(fullName);
    }

    /**
     * Modifies the stored content data
     * @param data New data to set
     * */
    public void setData(final byte[] data) {
        this.data = data;
    }

    /**
     * Returns the content data
     * @return Name of the content as byte array
     * */
    public byte[] getData() {
        return data;
    }

    // -------------------------------------
    // equals and hashCode
    // -------------------------------------

    @Generated
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ZipContent other = (ZipContent) obj;
        return Objects.equals(this.getFullName(), other.getFullName()) &&
                Objects.equals(this.getData(), other.getData());
    }

    @Generated
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
