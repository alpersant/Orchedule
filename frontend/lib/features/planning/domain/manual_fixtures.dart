class ManualFixtures {
  static List<Map<String,dynamic>> parse(String text,Set<String> teams){
    final lines=text.replaceAll('\r','').trim().split('\n');
    if(lines.isEmpty||lines.first.trim()!='round,homeTeamId,awayTeamId')throw const FormatException('La plantilla debe empezar con round,homeTeamId,awayTeamId');
    final groups=<int,List<Map<String,String>>>{};
    for(var i=1;i<lines.length;i++){
      if(lines[i].trim().isEmpty)continue;
      final c=lines[i].split(',').map((s)=>s.trim()).toList();
      final n=c.length==3?int.tryParse(c[0]):null;
      if(n==null||n<1||n>126||!teams.contains(c[1])||!teams.contains(c[2])||c[1]==c[2])throw FormatException('Fila ${i+1}: revisa la jornada y los equipos.');
      groups.putIfAbsent(n,()=>[]).add({'homeTeamId':c[1],'awayTeamId':c[2]});
    }
    if(groups.isEmpty)throw const FormatException('Añade los enfrentamientos de al menos una jornada.');
    final result=<Map<String,dynamic>>[];
    for(var n=1;n<=groups.length;n++){
      final games=groups[n];if(games==null)throw FormatException('Falta la jornada $n.');
      final used=<String>{};
      for(final g in games){if(!used.add(g['homeTeamId']!)||!used.add(g['awayTeamId']!))throw FormatException('Jornada $n: un equipo tiene más de un partido.');}
      final absent=teams.difference(used);
      if(absent.length!=teams.length%2)throw FormatException('Jornada $n: deben jugar todos; si son impares descansa exactamente uno.');
      result.add({'games':games,'byeTeamId':absent.isEmpty?null:absent.single});
    }
    return result;
  }
}
