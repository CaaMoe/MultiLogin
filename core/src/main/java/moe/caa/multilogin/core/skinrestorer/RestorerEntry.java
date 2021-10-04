package moe.caa.multilogin.core.skinrestorer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RestorerEntry {
    private final UUID online_uuid;
    private String current_skin_url;
    private String restorer_data;
}
