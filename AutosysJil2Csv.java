import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutosysJil2Csv {
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("usage: JIL2CSV jil-file csv-file");
			System.exit(0);
		}
		
		String jilFilename = args[0];
		String csvFilename = args[1];
		
		Path jilFilepath = Paths.get(jilFilename);
		Path csvFilepath = Paths.get(csvFilename);
		
		if (!Files.exists(jilFilepath)) {
			System.out.println("Cannot find JIL file!");
			System.exit(0);
		}
		
		if (Files.exists(csvFilepath)) {
			System.out.println("CSV file already exists! will delete it!");
			Files.delete(csvFilepath);
		}
		
		JIL2CSV.convert(jilFilename, csvFilename);
	}
	
	public static void convert(String jilFile, String csvFile) {
		System.out.println("Start converting JIL " + jilFile + "...");
		
		BufferedReader jilReader = null;
		BufferedWriter csvWriter = null;
		int totalJobs = 0;
	
		try {
			jilReader = new BufferedReader(new FileReader(jilFile));
			csvWriter = new BufferedWriter(new FileWriter(csvFile));
			
			// create a temp file
			File temp = File.createTempFile("myjil", ".tmp");
			BufferedWriter tempWriter = new BufferedWriter(new FileWriter(temp));
		
			// preprocessing
			String line;
			while ((line = jilReader.readLine()) != null) {
				line = line.replace("job_type:", "\r\njob_type:");
				line = line.trim();
				tempWriter.write(line);
				tempWriter.newLine();
			}
			
			tempWriter.close();
			jilReader.close();
			
			// default parameters, if I missed any or CA adds any, they can go anywhere in here 
			// Just remember to change the output string below
			LinkedHashMap<String, String> jobConfigurationMap = new LinkedHashMap<>();
			jobConfigurationMap.put("insert_job", "");
			jobConfigurationMap.put("job_type", "");
			jobConfigurationMap.put("box_name", "");
			jobConfigurationMap.put("command", "");
			jobConfigurationMap.put("machine", "");
			jobConfigurationMap.put("owner", "");
			jobConfigurationMap.put("permission", "");
			jobConfigurationMap.put("date_conditions", "");
			jobConfigurationMap.put("days_of_week", "");
			jobConfigurationMap.put("run_calendar", "");
			jobConfigurationMap.put("start_times", "");
			jobConfigurationMap.put("start_mins", "");
			jobConfigurationMap.put("run_window", "");
			jobConfigurationMap.put("condition", "");
			jobConfigurationMap.put("description", "");
			jobConfigurationMap.put("term_run_time", "");
			jobConfigurationMap.put("box_terminator", "");
			jobConfigurationMap.put("job_terminator", "");
			jobConfigurationMap.put("std_out_file", "");
			jobConfigurationMap.put("std_err_file", "");
			jobConfigurationMap.put("min_run_alarm", "");
			jobConfigurationMap.put("max_run_alarm", "");
			jobConfigurationMap.put("alarm_if_fail", "");
			jobConfigurationMap.put("max_exit_status", "");
			jobConfigurationMap.put("chk_files", "");
			jobConfigurationMap.put("profile", "");
			jobConfigurationMap.put("job_load", "");
			jobConfigurationMap.put("priority", "");
			jobConfigurationMap.put("auto_delete", "");
			jobConfigurationMap.put("group", "");
			jobConfigurationMap.put("application", "");
			jobConfigurationMap.put("exclude_calendar", "");
			jobConfigurationMap.put("group", "");
			jobConfigurationMap.put("box_success", "");
			jobConfigurationMap.put("max_exit_success", "");
			
			// output the header
			String jobParamJoin = jobConfigurationMap.keySet().stream().collect(Collectors.joining(","));
			csvWriter.write(jobParamJoin);
			jobParamJoin = null;
			// csvWriter.newLine();
			
			LinkedHashMap<String, String> jobParameterMap = new LinkedHashMap<>();
			
			jilReader = new BufferedReader(new FileReader(temp));
			while ((line = jilReader.readLine()) != null) {
				if (line.trim().equals("")) {
					jobParamJoin = null;
				} else if (line.startsWith("/*")) {
					jobParamJoin = jobParameterMap.values().stream().collect(Collectors.joining(","));
					
					csvWriter.write(jobParamJoin);
					csvWriter.newLine();
					
					totalJobs ++;
					
					// clear the buffer
					jobParameterMap.clear();
					jobParameterMap.putAll(jobConfigurationMap);
				} else {
					String[] values = line.split(": ");
					jobParameterMap.put(values[0], escapeCsv(values[1]));
				}
			}
			
			jobParamJoin = jobParameterMap.values().stream().collect(Collectors.joining(","));
			csvWriter.write(jobParamJoin);
			
			jilReader.close();
			csvWriter.close();

			temp.delete();			
			
			System.out.println("Total " + totalJobs + " jobs are converted.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
	}
	
	public static String escapeCsv(String value) {
		StringBuilder sb = new StringBuilder();
		
		if (value.indexOf("\"") != -1 || value.indexOf(",") != -1) {
			value = value.replaceAll("\"", "\"\"");
			sb.append("\"");
			sb.append(value);
			sb.append("\"");
		} else {
			sb.append(value);
		}
		
		return sb.toString();
	}
}
