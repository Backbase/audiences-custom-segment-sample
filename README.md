<p align="center">
  <img width="120" src="Backbase.png?raw=true" alt="Backbase Logo">
</p>

# Custom Segment Sample

Custom Segment Sample - is a demonstration of custom segment implementation.

## Configuration

Service configuration is under `src/main/resources/application.yaml`.

### Segments configuration
You can specify list of segments using `backbase.custom-segment-sample.segments.definitions` configuration property.

### CSV configuration
| Property                                                  | Default        | Description                                                    | 
|-----------------------------------------------------------|----------------|----------------------------------------------------------------|
| `backbase.custom-segment-sample.csv.delimiter`            | `,`            | The delimiter character of the recommendation report CSV file. |
| `backbase.custom-segment-sample.csv.headers.userId`       | `Customer_Num` | Name of the column containing **External User ID**             |
| `backbase.custom-segment-sample.csv.headers.category`     | `Category`     | Name of the column containing Category name                    | 
| `backbase.custom-segment-sample.csv.headers.sub-category` | `Sub_Category` | Name of the column containing Sub-category name                |
