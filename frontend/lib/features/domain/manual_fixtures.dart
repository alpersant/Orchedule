class ManualFixtures {
  static List<Map<String,dynamic>> parse(String text,Set<String> teams){
    final lines=text.replaceAll('\r','').trim().split('\n');
    if(lines.isEmpty||lines.first.trim()!='round,homeTeamId,awayTeamId')throw const FormatException('La plantilla debe empezar con round,homeTeamId,awayTeamId');
    final groups=<int,List<Map<String,String>>>{};
    for(var i=1;i<lines.length;i++){
      if(lines[i].trim().isEmpty)continue;
      final cells=lines[i].split(',').map((s)=>s.trim()).toList();
      final n=cells.length==3?int.tryParse(cells[0]):null;
      if(n==null||n<1||n>126||!teams.contains(cells[1])||!teams.contains(cells[2])||cells[1]==cells[2])throw FormatException('Fila ${i+1}: revisa la jornada y los equipos.');
      groups.putIfAbsent(n,()=>[]).add({'homeTeamId':cells[1],'awayTeamId':cells[2]});
    }
    if(groups.isEmpty)throw const FormatException('Añade los enfrentamientos de al menos una jornada.');
    final result=<Map<String,dynamic>>[];
    for(var n=1;n<=groups.length;n++){
      final games=groups[n];if(games==null)throw FormatException('Falta la jornada $n.');
      final used=<String>{};
      for(final g in games){if(!used.add(g['homeTeamId']!)||!used.add(g['awayTeamId']!))throw FormatException('Jornada $n: un equipo tiene más de un partido.');}
      final absent=teams.difference(used);
      if(absent.length!=teams.length%2)throw FormatException('Jornada $n: deben jugar todos los equipos; si son impares descansa exactamente uno.');
      result.add({'games':games,'byeTeamId':absent.isEmpty?null:absent.single});
    }
    return result;
  }
}
