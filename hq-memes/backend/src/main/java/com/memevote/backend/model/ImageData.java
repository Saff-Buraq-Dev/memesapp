package com.memevote.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Table(name = "image_data")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"data"})
@EqualsAndHashCode(of = {"id", "name", "type"})
public class ImageData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String type;
    
    @Lob
    @Column(name = "data", columnDefinition = "LONGBLOB")
    private byte[] data;
    
    @Column(name = "file_size")
    private Long fileSize;
}
